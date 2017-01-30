package tc.oc.pgm.debug;

import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.time.Duration;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.debug.LeakDetector;
import tc.oc.pgm.events.MatchUnloadEvent;
import tc.oc.pgm.events.PlayerLeaveMatchEvent;

public class PGMLeakListener implements PluginFacet, Listener {

    private static final Duration DEADLINE = Duration.ofSeconds(10);

    private final LeakDetector leakDetector;

    @Inject PGMLeakListener(LeakDetector leakDetector) {
        this.leakDetector = leakDetector;
    }

    @EventHandler
    public void onCycle(MatchUnloadEvent event) {
        leakDetector.expectRelease(event.getMatch(), DEADLINE, true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveMatchEvent event) {
        leakDetector.expectRelease(event.getPlayer(), DEADLINE, true);
    }
}
