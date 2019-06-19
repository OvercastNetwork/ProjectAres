package tc.oc.pgm.legacy;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.minecraft.protocol.MinecraftVersion;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScope;
import javax.inject.Inject;

@ListenerScope(MatchScope.RUNNING)
public class LegacyPlayerFacet implements MatchPlayerFacet, Listener {

    private final Audiences audiences;

    @Inject LegacyPlayerFacet(Audiences audiences) {
        this.audiences = audiences;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getVehicle() instanceof Boat && event.getActor() instanceof Player) {
            Player player = (Player)event.getActor();
            if (MinecraftVersion.lessThan(MinecraftVersion.MINECRAFT_1_9, player.getProtocolVersion())) {
                event.setCancelled(true);
                audiences.get(player).sendMessage(new WarningComponent("version.too.old.boats"));
            }
        }
    }
}
