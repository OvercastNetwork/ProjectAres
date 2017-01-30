package tc.oc.pgm.tracker;

import javax.annotation.Nullable;

import org.bukkit.block.Block;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.tracker.damage.PhysicalInfo;
import tc.oc.pgm.tracker.damage.TrackerInfo;

public interface BlockResolver {

    PhysicalInfo resolveBlock(Block block);

    @Nullable TrackerInfo resolveInfo(Block block);

    default @Nullable <T extends TrackerInfo> T resolveInfo(Block block, Class<T> infoType) {
        TrackerInfo info = resolveInfo(block);
        return infoType.isInstance(info) ? infoType.cast(info) : null;
    }

    @Nullable ParticipantState getOwner(Block block);
}
