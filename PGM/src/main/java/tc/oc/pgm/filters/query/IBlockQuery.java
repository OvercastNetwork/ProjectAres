package tc.oc.pgm.filters.query;

import org.bukkit.block.BlockState;

public interface IBlockQuery extends IMatchQuery, ILocationQuery, IMaterialQuery {

    BlockState getBlock();

    @Override
    default int randomSeed() {
        return getBlock().hashCode();
    }
}
