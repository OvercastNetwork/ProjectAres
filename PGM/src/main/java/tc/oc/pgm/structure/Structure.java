package tc.oc.pgm.structure;

import org.bukkit.World;
import org.bukkit.region.BlockRegion;
import org.bukkit.util.Vector;
import tc.oc.pgm.features.Feature;

/**
 * Created from a {@link StructureDefinition} for a specific {@link org.bukkit.World}.
 */
public interface Structure extends Feature<StructureDefinition> {

    /**
     * Return a {@link BlockRegion} containing only the blocks
     * that were copied from the {@link org.bukkit.World} for this
     * structure when it was loaded.
     *
     * This may be a subset of the {@link tc.oc.pgm.regions.Region}
     * from the structure's {@link StructureDefinition}.
     */
    BlockRegion dynamicBlocks();

    /**
     * Place this structure in its origin world, offset by the given delta.
     */
    void place(World world, Vector offset);
}
