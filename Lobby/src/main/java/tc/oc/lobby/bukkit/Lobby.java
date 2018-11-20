package tc.oc.lobby.bukkit;

import javax.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import tc.oc.commons.bukkit.inject.BukkitPluginManifest;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.commons.bukkit.teleport.NavigatorInterface;
import tc.oc.inject.ProtectedBinder;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.Gizmos;
import tc.oc.lobby.bukkit.listeners.EnvironmentControlListener;

public class Lobby extends JavaPlugin implements Listener {

    private static Lobby lobby;
    public final static String BUNGEE_CHANNEL = "BungeeCord";

    public Lobby() { lobby = this; }
    public static Lobby get() { return lobby; }

    @Inject private NavigatorInterface navigatorInterface;
    @Inject private MapdevLogger mapdevLogger;

    @Override
    public void configure(ProtectedBinder binder) {
        binder.install(new BukkitPluginManifest());
        binder.install(new LobbyManifest());
    }

    @Override
    public void onEnable() {
        // Ensure parsing errors show in the console on Lobby servers,
        // since PGM is not around to do that.
        mapdevLogger.setUseParentHandlers(true);

        this.getServer().getPluginManager().registerEvents(new EnvironmentControlListener(), this);
        this.getServer().getPluginManager().registerEvents(new Gizmos(), this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, BUNGEE_CHANNEL);

        this.setupScoreboard();
        this.loadConfig();

        for(Gizmo gizmo : Gizmos.gizmos) {
            Bukkit.getPluginManager().addPermission(new Permission(gizmo.getPermissionNode(), PermissionDefault.FALSE));
        }

        Settings settings = new Settings(this);
        settings.register();

        navigatorInterface.setOpenButtonSlot(Slot.Hotbar.forPosition(0));
    }

    private void setupScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for(Team team : scoreboard.getTeams()) {
            team.unregister();
        }
        for(Objective objective : scoreboard.getObjectives()) {
            objective.unregister();
        }
    }

    private void loadConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();
    }
}
