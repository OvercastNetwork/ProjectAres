package tc.oc.pgm;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Provider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.inject.BukkitPluginManifest;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.commons.bukkit.teleport.NavigatorInterface;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.inject.ProtectedBinder;
import tc.oc.minecraft.logging.BetterRaven;
import tc.oc.pgm.antigrief.CraftingProtect;
import tc.oc.pgm.channels.ChannelCommands;
import tc.oc.pgm.commands.MapCommands;
import tc.oc.pgm.commands.PollCommands;
import tc.oc.pgm.commands.RotationControlCommands;
import tc.oc.pgm.commands.RotationEditCommands;
import tc.oc.pgm.events.ConfigLoadEvent;
import tc.oc.pgm.ffa.FreeForAllCommands;
import tc.oc.pgm.fireworks.ObjectivesFireworkListener;
import tc.oc.pgm.goals.GoalCommands;
import tc.oc.pgm.listeners.FormattingListener;
import tc.oc.pgm.listeners.ItemTransferListener;
import tc.oc.pgm.logging.MapTagger;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.MapNotFoundException;
import tc.oc.pgm.mapratings.MapRatingsCommands;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchLoader;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.pollablemaps.PollableMaps;
import tc.oc.pgm.polls.PollListener;
import tc.oc.pgm.polls.PollManager;
import tc.oc.pgm.start.StartCommands;
import tc.oc.pgm.tablist.MatchTabManager;
import tc.oc.pgm.timelimit.TimeLimitCommands;
import tc.oc.pgm.tokens.gui.MainTokenButton;

import static com.google.common.base.Preconditions.checkState;

public final class PGM extends JavaPlugin {

    private static PGM pgm;
    public PGM() {
        checkState(pgm == null);
        pgm = this;
    }
    public static PGM get() { return pgm; }

    @Override
    public void configure(ProtectedBinder binder) {
        binder.install(new BukkitPluginManifest());
        binder.install(new PGMManifest());
    }

    @Inject private CommandRegistry commands;
    @Inject private MapLibrary mapLibrary;
    @Inject private Optional<BetterRaven> raven;
    @Inject private MapdevLogger mapdevLogger;
    @Inject private NavigatorInterface navigatorInterface;
    @Inject private Provider<MatchLoader> matchLoader;

    private MatchManager matchManager;

    public static MatchManager getMatchManager() {
        return pgm == null ? null : pgm.matchManager;
    }

    public static MatchManager needMatchManager() {
        MatchManager mm = getMatchManager();
        if(mm == null) {
            throw new IllegalStateException("PGMMatchManager is not available");
        }
        return mm;
    }

    public Logger getRootMapLogger() {
        return mapdevLogger;
    }

    private PollManager pollManager;

    public static PollManager getPollManager() {
        return pgm == null ? null : pgm.pollManager;
    }

    private PollableMaps pollableMaps;

    public static PollableMaps getPollableMaps() {
        return pgm == null ? null : pgm.pollableMaps;
    }

    public MapLibrary getMapLibrary() {
        return mapLibrary;
    }

    private MatchTabManager matchTabManager;

    private void setupSentry() {
        // Tag Sentry events with the current map
        raven.ifPresent(raven -> raven.addHelper(new MapTagger(matchManager)));
    }

    @Override
    public void onEnable() {
        matchManager = injector().getInstance(MatchManager.class);

        getServer().getConsoleSender().addAttachment(this, Permissions.MAPDEV, true);
        getServer().getConsoleSender().addAttachment(this, Permissions.MAPERRORS, true);

        // Create objects that listen for config changes
        Config.PlayerList.register();

        // Copy the default configuration
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();
        this.setupSentry();
        this.setupCommands();

        try {
            matchManager.loadNewMaps();
        } catch(MapNotFoundException e) {
            this.getLogger().log(Level.SEVERE, "PGM could not load any maps, server will shut down", e);
            this.getServer().shutdown();
            return;
        }

        this.pollManager = new PollManager(this);
        this.pollableMaps = new PollableMaps();

        this.registerListeners();

        // cycle match in 0 ticks so it loads after other plugins are done
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if(PGM.this.matchManager.cycleToNext(null, true, true) == null) {
                getLogger().severe("Failed to load an initial match, shutting down");
                getServer().shutdown();
            }
        }, 0);

        if(Config.Broadcast.periodic()) {
            // periodically notify people of what map they're playing
            this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    Match match = matchLoader.get().getCurrentMatch();
                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + PGMTranslations.get().t("broadcast.currentlyPlaying", Bukkit.getConsoleSender(), match.getMap().getInfo().getShortDescription(Bukkit.getConsoleSender()) + ChatColor.DARK_PURPLE));
                    for (MatchPlayer player : match.getPlayers()) {
                        player.sendMessage(ChatColor.DARK_PURPLE + PGMTranslations.t("broadcast.currentlyPlaying", player, match.getMap().getInfo().getShortDescription(player.getBukkit()) + ChatColor.DARK_PURPLE));
                    }
                }
            }, 20, Config.Broadcast.frequency() * 20);
        }

        if(Config.PlayerList.enabled()) {
            this.matchTabManager = new MatchTabManager(this);
            this.matchTabManager.enable();
        }

        // Would rather configure this with a Guice binding, but we can't as long as
        // PGM modules are installed on lobby servers, because the bindings will conflict.
        navigatorInterface.setOpenButtonSlot(Slot.Hotbar.forPosition(7));

        new MainTokenButton();
    }

    @Override
    public void onDisable() {
        if(this.matchTabManager != null) {
            this.matchTabManager.disable();
            this.matchTabManager = null;
        }

        matchLoader.get().unloadAllMatches();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.getServer().getPluginManager().callEvent(new ConfigLoadEvent(this.getConfig()));
    }

    private void setupCommands() {
        commands.register(MapCommands.class);
        commands.register(ChannelCommands.class);
        commands.register(PollCommands.class);
        commands.register(RotationEditCommands.RotationEditParent.class);
        commands.register(RotationControlCommands.RotationControlParent.class);
        commands.register(TimeLimitCommands.class);
        commands.register(MapRatingsCommands.class);
        commands.register(GoalCommands.class);
        commands.register(StartCommands.class);
        commands.register(FreeForAllCommands.Parent.class);
    }

    private void registerListeners() {
        this.registerEvents(new PollListener(this.pollManager, this.matchManager));
        this.registerEvents(new FormattingListener());
        this.registerEvents(new CraftingProtect());
        this.registerEvents(new ObjectivesFireworkListener());
        this.registerEvents(new ItemTransferListener());
    }

    public void registerEvents(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }
}
