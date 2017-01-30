package tc.oc.pgm.regions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.commons.bukkit.util.BlockVectorSet;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.BlockQuery;
import tc.oc.pgm.utils.MaterialPattern;

import static tc.oc.pgm.map.ProtoVersions.REGION_FIX_VERSION;

/**
 * Region represented by a list of single blocks.  This will check if a point
 * is inside the block at all.
 */
public class FiniteBlockRegion extends Region.Impl {
    private final BlockVectorSet positions;
    private final Cuboid bounds;

    public FiniteBlockRegion(Collection<BlockVector> positions) {
        this.positions = BlockVectorSet.of(positions);

        // calculate AABB
        final Vector min = new Vector(Double.MAX_VALUE);
        final Vector max = new Vector(-Double.MAX_VALUE);

        for(BlockVector pos : this.positions) {
            min.setX(Math.min(min.getX(), pos.getBlockX()));
            min.setY(Math.min(min.getY(), pos.getBlockY()));
            min.setZ(Math.min(min.getZ(), pos.getBlockZ()));

            max.setX(Math.max(max.getX(), pos.getBlockX() + 1));
            max.setY(Math.max(max.getY(), pos.getBlockY() + 1));
            max.setZ(Math.max(max.getZ(), pos.getBlockZ() + 1));
        }

        this.bounds = Cuboid.between(min, max);
    }

    @Inspect
    public int size() {
        return positions.size();
    }

    @Override
    public boolean contains(Vector point) {
        return bounds.contains(point) && positions.contains(point.toBlockVector());
    }

    @Override
    public boolean canGetRandom() {
        return true;
    }

    @Override
    public boolean isBlockBounded() {
        return true;
    }

    @Override
    public Cuboid getBounds() {
        return this.bounds;
    }

    @Override
    public Vector getRandom(Random random) {
        final BlockVector randomBlock = positions.chooseRandom(random);
        double dx = random.nextDouble();
        double dy = random.nextDouble();
        double dz = random.nextDouble();
        return randomBlock.add(dx, dy, dz);
    }

    @Override
    public Iterator<BlockVector> getBlockVectorIterator() {
        return positions.mutableIterator();
    }

    @Override
    public Set<BlockVector> getBlockVectors() {
        return positions;
    }

    @Override
    public Stream<BlockVector> blockPositions() {
        return positions.stream();
    }

    public static class Factory {
        private final SemanticVersion mapProto;

        public Factory(SemanticVersion mapProto) {
            this.mapProto = mapProto;
        }

        public FiniteBlockRegion fromWorld(Region region, World world, MaterialPattern...materials) {
            return fromWorld(region, world, Arrays.asList(materials));
        }

        public FiniteBlockRegion fromWorld(Region region, World world, Collection<MaterialPattern> materials) {
            return fromWorld(region, world, (Predicate<Block>) block -> materials.stream().anyMatch(m -> m.matches(block.getState().getMaterialData())));
        }

        public FiniteBlockRegion fromWorld(Region region, World world, Filter filter) {
            return fromWorld(region, world, (Predicate<Block>) block -> filter.query(new BlockQuery(block)).isAllowed());
        }

        public FiniteBlockRegion fromWorld(Region region, World world, Predicate<Block> filter) {
            if(region instanceof CuboidRegion && mapProto.isOlderThan(REGION_FIX_VERSION)) {
                // Due to an old bug, legacy maps have cuboids that are one block too big
                region = new CuboidRegion(region.getBounds().minimum(), region.getBounds().maximum().plus(1, 1, 1));
            }

            return new FiniteBlockRegion(region.blockPositions()
                                               .filter(pos -> filter.test(world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ())))
                                               .collect(Collectors.toCollection(BlockVectorSet::new)));
        }
    }
}
