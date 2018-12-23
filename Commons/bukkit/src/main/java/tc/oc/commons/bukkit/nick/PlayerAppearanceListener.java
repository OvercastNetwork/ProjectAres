package tc.oc.commons.bukkit.nick;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Listens for events that require player names and/or appearances to be refreshed.
 */
@Singleton
public class PlayerAppearanceListener implements Listener, PluginFacet {

    private final PlayerAppearanceChanger playerAppearanceChanger;

    @Inject PlayerAppearanceListener(PlayerAppearanceChanger playerAppearanceChanger) {
        this.playerAppearanceChanger = playerAppearanceChanger;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onIdentityChange(PlayerIdentityChangeEvent event) {
        playerAppearanceChanger.refreshPlayer(event.getPlayer(), event.getNewIdentity());
    }

    /**
     * When a player logs in, refresh their own appearance, and the appearance of all other players for them.
     * This must run after {@link IdentityProviderImpl#applyNicknameOnLogin}
     *
     * Note: this needs to happen in {@link PlayerJoinEvent} rather than {@link PlayerLoginEvent},
     * because the latter fires before the player is added to {@link Server#getOnlinePlayers},
     * and the nickname packet filter in SportBukkit uses that list to lookup players from PacketPlayOutScoreboardTeam.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void refreshNamesOnLogin(PlayerJoinEvent event) {
        playerAppearanceChanger.refreshPlayer(event.getPlayer());
        playerAppearanceChanger.refreshViewer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        playerAppearanceChanger.cleanupAfterPlayer(event.getPlayer());
    }
}
