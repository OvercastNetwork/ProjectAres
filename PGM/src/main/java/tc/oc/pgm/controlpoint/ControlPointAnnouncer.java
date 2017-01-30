package tc.oc.pgm.controlpoint;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.controlpoint.events.ControllerChangeEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.LOADED)
public class ControlPointAnnouncer implements Listener {
    private final Match match;

    public ControlPointAnnouncer(Match match) {
        this.match = match;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOwnerChange(ControllerChangeEvent event) {
        if(event.getControlPoint().isVisible()) {
            final Component point = new Component(event.getControlPoint().getComponentName(), ChatColor.WHITE);
            final Component message = new Component(ChatColor.GRAY);
            final Competitor before = event.getOldController();
            final Competitor after = event.getNewController();
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
