package tc.oc.pgm.modules;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.RUNNING)
public class FlyingBoatMatchModule extends MatchModule implements Listener {

    public FlyingBoatMatchModule(Match match) {
        super(match);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBoatPlace(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Boat) {
            vehicle.setGravity(false);
        }
    }
}
