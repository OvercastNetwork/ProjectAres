package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.HashSet;
import java.util.Set;

public class BoatMutation extends KitMutation {

    final static FreeItemKit BOAT = new FreeItemKit(item(Material.BOAT));

    final Set<Vehicle> vehicles;

    public BoatMutation(Match match) {
        super(match, true, BOAT);
        vehicles = new HashSet<Vehicle>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBoatPlace(VehicleCreateEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Boat) {
            vehicle.setGravity(false);
            vehicles.add(event.getVehicle());
        }
    }

    private void removeBoatGravity() {
        for (Vehicle vehicle: vehicles) {
            vehicle.setGravity(true);
        }
        vehicles.clear();
    }

    @Override
    public void disable() {
        super.disable();
        removeBoatGravity();
    }

}
