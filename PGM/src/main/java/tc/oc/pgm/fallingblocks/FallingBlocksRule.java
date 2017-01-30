package tc.oc.pgm.fallingblocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.BlockQuery;
import tc.oc.commons.bukkit.util.Materials;

import javax.annotation.Nullable;

public class FallingBlocksRule {
    public static final int DEFAULT_DELAY = 2;

    public final Filter fall;
    public final Filter stick;
    public final int delay;

    public FallingBlocksRule(Filter fall, Filter stick, int delay) {
        this.fall = fall;
        this.stick = stick;
        this.delay = delay;
    }

    public boolean canFall(Block block) {
        return this.canFall(block.getState());
    }

    public boolean canFall(BlockState block) {
        // Can't spawn falling air blocks
        return block.getMaterial() != Material.AIR &&
               fall.query(new BlockQuery(block))
                   .toBoolean(block.getMaterial().hasGravity());
    }

    public boolean canSupport(BlockState supporter) {
        return supporter.getMaterial() != Material.AIR &&
               stick.query(new BlockQuery(supporter)).isAllowed();
    }

    public boolean canSupport(Block supporter) {
        // Supportive air would be pointless since nothing could ever fall
        return supporter.getType() != Material.AIR &&
               stick.query(new BlockQuery(supporter)).isAllowed();
    }

    /**
     * Test if the given block is supportive from the given direction,
     * either because this rule makes the block sticky, or because it's
     * a solid block supporting from below.
     */
    public boolean canSupport(@Nullable Block supporter, BlockFace from) {
        if(supporter == null || supporter.getType() == Material.AIR) return false;
        if(from == BlockFace.DOWN && Materials.canSupportBlocks(supporter.getType())) return true;
        return canSupport(supporter);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
               "{fall=" + this.fall +
               " stick=" + this.stick +
               " delay=" + this.delay +
               "}";
    }
}
