package tc.oc.pgm.renewable;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;
import org.bukkit.geometry.Cuboid;
import org.bukkit.geometry.Vec3;
import tc.oc.commons.core.util.DefaultMapAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Array-backed volume of block states (id:data pairs)
 * with fixed size and location. All positions in or out
 * are in world coordinates. Initially filled with air.
 */
public class BlockImage {
    private final World world;
    private final Vec3 origin;
    private final Vec3 size;
    private final Cuboid bounds;
    private final int volume;
    private final short[] blockIds;
    private final byte[] blockData;
    private final Map<MaterialData, Integer> blockCounts;

    public BlockImage(World world, Cuboid bounds) {
        this(world, bounds, false);
    }

    public BlockImage(World world, Cuboid bounds, boolean keepCounts) {
        this.world = world;
        this.bounds = bounds;
        this.origin = this.bounds.minimumBlockInside();
        this.size = this.bounds.blockSize();
        this.volume = Math.max(0, this.bounds.blockVolume());

        blockIds = new short[this.volume];
        blockData = new byte[this.volume];

        if(keepCounts) {
            this.blockCounts = new DefaultMapAdapter<>(new HashMap<MaterialData, Integer>(), 0);
        } else {
            this.blockCounts = null;
        }
    }

    /**
     * @return The boundaries of this image in world coordinates
     */
    public Cuboid getBounds() {
        return bounds;
    }

    public Map<MaterialData, Integer> getBlockCounts() {
        return blockCounts;
    }

    private int offset(BlockVector pos) {
        if(!this.bounds.containsBlock(pos)) {
            throw new IndexOutOfBoundsException("Block is not inside this BlockImage");
        }

        return (pos.coarseZ() - this.origin.coarseZ()) * this.size.coarseX() * this.size.coarseY() +
               (pos.coarseY() - this.origin.coarseY()) * this.size.coarseX() +
               (pos.coarseX() - this.origin.coarseX());
    }

    /**
     * @param pos   Block position in world coordinates
     * @return      Block state saved in this image at the given position
     */
    @SuppressWarnings("deprecation")
    public MaterialData get(BlockVector pos) {
        int offset = this.offset(pos);
        return new MaterialData(this.blockIds[offset], this.blockData[offset]);
    }

    @SuppressWarnings("deprecation")
    public BlockState getState(BlockVector pos) {
        int offset = this.offset(pos);
        BlockState state = pos.toLocation(this.world).getBlock().getState();
        state.setTypeId(this.blockIds[offset]);
        state.setRawData(this.blockData[offset]);
        return state;
    }

    /**
     * Set every block in this image to its current state in the world
     */
    @SuppressWarnings("deprecation")
    public void save() {
        if(this.blockCounts != null) {
            this.blockCounts.clear();
        }

        int offset = 0;
        for(Vec3 v : this.bounds.blockRegion().mutableIterable()) {
            Block block = this.world.getBlockAt(v.coarseX(),
                                                v.coarseY(),
                                                v.coarseZ());
            this.blockIds[offset] = (short) block.getTypeId();
            this.blockData[offset] = block.getData();
            ++offset;

            if(this.blockCounts != null) {
                MaterialData md = block.getState().getData();
                this.blockCounts.put(md, this.blockCounts.get(md) + 1);
            }
        }
    }

    /**
     * Copy the block at the given position from the image to the world
     * @param pos   Block position in world coordinates
     */
    @SuppressWarnings("deprecation")
    public void restore(BlockVector pos) {
        int offset = this.offset(pos);
        pos.toLocation(this.world).getBlock().setTypeIdAndData(this.blockIds[offset],
                                                               this.blockData[offset],
                                                               true);
    }
}
