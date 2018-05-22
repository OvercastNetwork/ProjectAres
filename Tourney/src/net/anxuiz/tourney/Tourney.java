package net.anxuiz.tourney;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import net.anxuiz.tourney.listener.KDMListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import tc.oc.api.docs.Tournament;
import tc.oc.api.tourney.TournamentService;
import tc.oc.api.tourney.TournamentStore;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.inject.BukkitPluginManifest;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.inject.ProtectedBinder;
import tc.oc.minecraft.scheduler.Sync;

import static com.google.common.base.Preconditions.checkNotNull;

public class Tourney extends JavaPlugin {

    private static Tourney inst;

    private @Nullable KDMSession kdmSession;
    private @Nullable KDMListener kdmListener;

    @Inject private @Sync Flexecutor executor;
    @Inject private TournamentService tournamentService;
    @Inject private TournamentStore tournamentStore;
    @Inject private Audiences audiences;
    @Inject private Provider<Tournament> tournamentProvider;
    @Inject private Provider<MatchManager> matchManagerProvider;
    @Inject private Provider<KDMSession> kdmSessionProvider;

    public static Tourney get() {
        return checkNotNull(inst);
    }

    public Tourney() {
        inst = this;
    }

    @Override
    public boolean isActive() {
        return Config.tournamentID() != null;
    }

    @Override
    public void configure(ProtectedBinder binder) {
        binder.install(new BukkitPluginManifest());
        binder.install(new TourneyManifest());
    }

    public Tournament getTournament() {
        return tournamentProvider.get();
    }

    public void recordMatch(String matchId) {
        executor.callback(
            tournamentService.recordMatch(tournamentProvider.get(), matchId),
            response -> {
                if(!response.entrants().isEmpty()) {
                    final TranslatableComponent message = new TranslatableComponent(
                        "tourney.recordedMatch",
                        new Component(response.match()._id(), ChatColor.AQUA),
                        new Component(response.match().map().name(), ChatColor.LIGHT_PURPLE),
                        new ListComponent(response.entrants()
                                                  .stream()
                                                  .map(entrant -> new Component(entrant.team().name(), ChatColor.YELLOW)))
                    );

                    audiences.get(Bukkit.getConsoleSender()).sendMessage(message);
                    audiences.permission(TourneyPermissions.REFEREE).sendMessage(message);
                }
            }
        );
    }

    public boolean isRecordQueued() {
        return matchManagerProvider.get().isRecordQueued();
    }

    public void setRecordQueued(boolean recordQueued) {
        matchManagerProvider.get().setRecordQueued(recordQueued);
    }

    public TourneyState getState() {
        return matchManagerProvider.get().getState();
    }

    public void setState(TourneyState state) {
        matchManagerProvider.get().setState(state);
    }


    public @Nullable KDMSession getKDMSession() {
        return this.kdmSession;
    }

    public void createKDMSession() {
        if(kdmSession == null) {
            getLogger().info("Creating KDM session");
            kdmSession = kdmSessionProvider.get();
            kdmListener = new KDMListener(kdmSession);
            eventRegistry().registerListener(kdmListener);
        }
    }

    public void clearKDMSession() {
        if(kdmSession != null) {
            eventRegistry().unregisterListener(kdmListener);
            kdmListener = null;
            kdmSession = null;
        }
    }

    public MatchManager getMatchManager() {
        return matchManagerProvider.get();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        this.clearKDMSession();
        inst = null;
    }

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        getLogger().info("Loading tournament " + tournamentProvider.get().name());

        if (!Config.betterSprintAllowed()) {
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BSprint", (s, player, bytes) -> player.kickPlayer(
                ChatColor.BLACK + "" + ChatColor.RED + ChatColor.RED + "It looks like you're using BetterSprint.\n\n" +
                ChatColor.RED + "We do not allow BetterSprint to be used for tournament matches.\n\n" +
                ChatColor.RED + "Please re-connect without BetterSprint enabled."
            ));
        }
    }
}
