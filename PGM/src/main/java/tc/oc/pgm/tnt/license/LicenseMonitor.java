package tc.oc.pgm.tnt.license;

import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.goals.Goal;
import tc.oc.pgm.goals.events.GoalCompleteEvent;

public class LicenseMonitor implements Listener, PluginFacet {

    private final LicenseBroker licenseBroker;

    @Inject LicenseMonitor(LicenseBroker licenseBroker) {
        this.licenseBroker = licenseBroker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onObjectiveComplete(GoalCompleteEvent<Goal> event) {
        event.getContributions()
             .forEach(contrib -> licenseBroker.grant(contrib.getPlayerState().getPlayerId(),
                                                     LicenseBroker.GrantReason.OBJECTIVES));
    }
}
