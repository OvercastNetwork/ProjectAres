package tc.oc.commons.bukkit.debug;

import java.time.Duration;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.model.ModelDispatcher;
import tc.oc.api.model.ModelListener;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.debug.LeakDetector;

public class LeakListener implements PluginFacet, Listener, ModelListener {

    private static final Duration DEADLINE = Duration.ofSeconds(10);

    private final LeakDetector leakDetector;
    private final BukkitUserStore userStore;

    @Inject LeakListener(LeakDetector leakDetector, BukkitUserStore userStore, ModelDispatcher modelDispatcher) {
        this.leakDetector = leakDetector;
        this.userStore = userStore;
        modelDispatcher.subscribe(this);
    }

    @HandleModel
    public void modelUpdated(@Nullable Model before, @Nullable Model after, Model latest) {
        if(before != null && before != after) {
            leakDetector.expectRelease(before, DEADLINE, true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogout(PlayerQuitEvent event) {
        leakDetector.expectRelease(event.getPlayer(), DEADLINE, true);
        leakDetector.expectRelease(userStore.tryUser(event.getPlayer()), DEADLINE, true);
        leakDetector.expectRelease(userStore.getSession(event.getPlayer()), DEADLINE, true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(WorldUnloadEvent event) {
        leakDetector.expectRelease(event.getWorld(), DEADLINE, true);
    }
}
