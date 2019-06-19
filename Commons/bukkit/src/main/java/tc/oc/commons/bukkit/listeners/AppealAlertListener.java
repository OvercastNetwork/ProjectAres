package tc.oc.commons.bukkit.listeners;

import javax.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.bukkit.format.MiscFormatter;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Nag staff about unread appeals when they login
 */
public class AppealAlertListener implements PluginFacet, Listener {

    private static final String PERMISSION = "projectares.appeals.alerts";

    private final MiscFormatter miscFormatter;
    private final Audiences audiences;

    @Inject private AppealAlertListener(MiscFormatter miscFormatter, Audiences audiences) {
        this.miscFormatter = miscFormatter;
        this.audiences = audiences;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLogin(UserLoginEvent event) {
        final int count = event.response().unread_appeal_count();
        if(count > 0 && event.getPlayer().hasPermission(PERMISSION)) {
            audiences.get(event.getPlayer()).sendMessage(
                new Component(ChatColor.RED)
                    .extra(miscFormatter.typePrefix("A"))
                    .translate("appealNotification.count",
                               new Component(count, ChatColor.AQUA),
                               new TranslatableComponent(count == 1 ? "misc.appeals.singular"
                                                                    : "misc.appeals.plural"))
            );
        }
    }
}
