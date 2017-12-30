package tc.oc.pgm.control;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.control.events.ControllableOwnerChangeEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;

/**
 * Announces changes of state and ownership of any controllable goal.
 */
@ListenerScope(MatchScope.LOADED)
public class ControllableGoalAnnouncer implements Listener {

    private final Match match;

    public ControllableGoalAnnouncer(Match match) {
        this.match = match;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOwnerChange(ControllableOwnerChangeEvent event) {
        if(event.controllable().isVisible()) {
            final Component point = new Component(event.controllable().getComponentName(), ChatColor.WHITE);
            final Component message = new Component(ChatColor.GRAY);
            final Competitor before = event.oldController();
            final Competitor after = event.newController();
            if(after == null) {
                message.translate("objective.lose",
                                  before.getComponentName(),
                                  point);
            } else if(before == null) {
                message.translate("objective.capture",
                                  after.getComponentName(),
                                  point);
            } else {
                message.translate("objective.take",
                                  after.getComponentName(),
                                  point,
                                  before.getComponentName());
            }
            match.sendMessage(message);
        }
    }

}
