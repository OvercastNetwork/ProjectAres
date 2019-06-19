package tc.oc.lobby.bukkit.gizmos.christmas;

import com.google.common.collect.Range;
import java.time.MonthDay;
import java.time.ZoneOffset;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public abstract class ChristmasGizmo extends Gizmo {

    public ChristmasGizmo(String name, String prefix, String description, Material icon) {
        super(name, prefix, description, icon, 0);
    }

    public abstract Range<MonthDay> freeRange();

    @Override
    public boolean canPurchase(Player player) {
        // HACK: Players can only have one Christmas gizmo
        MonthDay now = MonthDay.now(ZoneOffset.UTC);
        return (player.hasPermission("lobby.gizmo.buy.christmas") && !ownsAny(player)) || player.isOp() || (freeRange().contains(now));
    }

    private boolean ownsAny(Player player) {
        for (Gizmo gizmo : Gizmos.gizmos) {
            if (gizmo instanceof ChristmasGizmo && gizmo.ownsGizmo(player)) return true;
        }
        return false;
    }
}
