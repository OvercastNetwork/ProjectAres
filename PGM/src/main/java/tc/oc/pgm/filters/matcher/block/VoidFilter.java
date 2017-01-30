package tc.oc.pgm.filters.matcher.block;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IBlockQuery;
import tc.oc.pgm.listeners.WorldProblemMatchModule;

/**
 * Matches blocks that have only air/void below them
 */
public class VoidFilter extends TypedFilter.Impl<IBlockQuery> {

    @Override
    public boolean matches(IBlockQuery query) {
        final BlockState block = query.getBlock();
        return block.getY() == 0 ||
               (!query.getMatch().needMatchModule(WorldProblemMatchModule.class).wasBlock36(block.getX(), 0, block.getZ()) &&
                block.getWorld().getBlockAt(block.getX(), 0, block.getZ()).getType() == Material.AIR);
    }

    @Override
    public String toString() {
        return "VoidFilter{}";
    }
}
