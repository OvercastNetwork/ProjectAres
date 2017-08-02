package tc.oc.commons.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Player punched a block.
 */
public class BlockPunchEvent extends AdventureModeInteractEvent implements Cancellable {

    private final RayBlockIntersection intersection;

    public BlockPunchEvent(Player player, RayBlockIntersection intersection) {
        super(player, intersection.getBlock());
        this.intersection = intersection;
    }

    public RayBlockIntersection getIntersection() {
        return intersection;
    }
}
