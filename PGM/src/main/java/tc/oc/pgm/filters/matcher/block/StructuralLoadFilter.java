package tc.oc.pgm.filters.matcher.block;

import tc.oc.pgm.fallingblocks.FallingBlocksMatchModule;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IBlockQuery;

/**
 * NOTE: this is potentially a very EXPENSIVE filter to apply, so XML authors should take
 * care to avoid evaluating it whenever possible, by placing other filters above it. They
 * should be particularly careful not to apply it to any events that modify large amounts
 * of blocks all at once, such as explosions.
 *
 * The XML documentation should note all of this.
 */
public class StructuralLoadFilter extends TypedFilter.Impl<IBlockQuery> {

    private final @Inspect int threshold;

    public StructuralLoadFilter(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean matches(IBlockQuery query) {
        return query.module(FallingBlocksMatchModule.class)
                    .map(fbmm -> fbmm.countUnsupportedNeighbors(query.getBlock().getBlock(), threshold) >= threshold)
                    .orElseGet(() -> 0 >= threshold);
    }
}
