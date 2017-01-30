package tc.oc.pgm.filters.query;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.util.ImVector;
import tc.oc.pgm.PGM;
import tc.oc.pgm.match.Match;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A block query is canonically defined by a {@link World} and a set of integer block coordinates.
 * The other properties are created lazily, to gain a bit of efficiency when querying filters that
 * don't check them.
 */
public class BlockQuery implements IBlockQuery {

    private final World world;
    private final Match match;
    private final int x, y, z;
    private @Nullable BlockState block;
    private @Nullable Location location;
    private @Nullable ImVector blockCenter;
    private @Nullable MaterialData material;

    protected BlockQuery(World world, int x, int y, int z, @Nullable BlockState block) {
        this.world = checkNotNull(world);
        this.match = PGM.getMatchManager().getMatch(world);
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    public BlockQuery(Block block) {
        this(block.getWorld(), block.getX(), block.getY(), block.getZ(), null);
    }

    public BlockQuery(BlockState block) {
        this(block.getWorld(), block.getX(), block.getY(), block.getZ(), block);
    }

    @Override
    public Match getMatch() {
        return match;
    }

    @Override
    public BlockState getBlock() {
        if(block == null) {
            block = world.getBlockAt(x, y, z).getState();
        }
        return block;
    }

    @Override
    public Location getLocation() {
        if(location == null) {
            location = new Location(world, x, y, z);
        }
        return location;
    }

    @Override
    public ImVector blockCenter() {
        if(blockCenter == null) {
            blockCenter = ImVector.centerOf(x, y, z);
        }
        return blockCenter;
    }

    @Override
    public MaterialData getMaterial() {
        if(material == null) {
            material = getBlock().getMaterialData();
        }
        return material;
    }
}
