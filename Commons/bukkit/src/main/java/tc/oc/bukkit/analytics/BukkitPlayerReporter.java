package tc.oc.bukkit.analytics;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.minecraft.analytics.PlayerReporter;

public class BukkitPlayerReporter extends PlayerReporter implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void join(PlayerJoinEvent event) {
        join();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void leave(PlayerQuitEvent event) {
        leave();
    }
}
