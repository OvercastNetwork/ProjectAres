package tc.oc.commons.bukkit.listeners;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.users.UserService;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Saves the player's locale on {@link PlayerLocaleChangeEvent}, which fires when
 * the server receives a ClientSettings packet, and the locale is different from
 * the current value. The client always sends this packet just after connecting,
 * and Bungee also re-sends the packet on every server change.
 *
 * Because we initialize the locale from the DB on login, we normally won't get
 * this event. It will only fire when the player has actually changed their locale.
 */
@Singleton
public class LocaleListener implements Listener, PluginFacet {

    private final BukkitUserStore userStore;
    private final UserService userService;

    @Inject LocaleListener(BukkitUserStore userStore, UserService userService) {
        this.userStore = userStore;
        this.userService = userService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocaleChange(PlayerLocaleChangeEvent event) {
        userService.update(userStore.getUser(event.getPlayer()), (UserDoc.Locale) event::getNewLocale);
    }
}
