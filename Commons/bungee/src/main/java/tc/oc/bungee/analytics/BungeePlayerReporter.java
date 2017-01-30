package tc.oc.bungee.analytics;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import tc.oc.minecraft.analytics.PlayerReporter;

public class BungeePlayerReporter extends PlayerReporter implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PostLoginEvent event) {
        join();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void leave(PlayerDisconnectEvent event) {
        leave();
    }
}
