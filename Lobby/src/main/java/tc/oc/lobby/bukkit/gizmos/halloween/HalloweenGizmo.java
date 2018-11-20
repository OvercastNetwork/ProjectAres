package tc.oc.lobby.bukkit.gizmos.halloween;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public abstract class HalloweenGizmo extends Gizmo {

    public HalloweenGizmo(String name, String prefix, String description, Material icon) {
        super(name, prefix, description, icon, 0);
    }

    @Override
    public boolean canPurchase(Player player) {
        // HACK: Players can only have one hallow gizmo
        return (player.hasPermission("lobby.gizmo.buy.hallow") && !ownsAny(player)) || player.isOp();
    }

    private boolean ownsAny(Player player) {
        for (Gizmo gizmo : Gizmos.gizmos) {
            if (gizmo instanceof HalloweenGizmo && gizmo.ownsGizmo(player)) return true;
        }
        return false;
    }
}
