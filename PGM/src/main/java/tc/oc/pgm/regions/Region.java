package tc.oc.pgm.regions;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Iterators;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.geometry.Vec3;
import org.bukkit.util.BlockVector;
import org.bukkit.geometry.Cuboid;
import org.bukkit.util.ImVector;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.ChunkPosition;
import tc.oc.commons.core.util.Streams;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.ILocationQuery;

/**
 * Represents an arbitrary region in a Bukkit world.
 */
@FeatureInfo(name = "region")
public interface Region extends TypedFilter<ILocationQuery> {

    /**
     * @return The smallest cuboid that entirely contains this region
     */
    Cuboid getBounds();

    /**
     * True only if this region contains no points at all times.
     *
     * Note that a zero-volume region is not necessarily empty.
     */
    default boolean isEmpty() {
        return false;
    }

    /**
     * True only if this region contains all points at all times.
     */
    default boolean isEverywhere() {
        return false;
    }

    @Override
    default boolean matches(ILocationQuery query) {
        return contains(query.blockCenter());
    }

    @Override
    default boolean isDynamic() {
        return true;
    }

    /**
     * Test if the region contains the given point
     */
    boolean contains(Vector point);

    default boolean contains(Vec3 point) {
        return contains(ImVector.copyOf(point.isFine() ? point.fineCopy()
                                                       : point.blockCenter()));
    }

    default boolean contains(BlockVector blockPos) {
        return contains(blockPos.blockCenter());
    }

    default boolean contains(Block block) {
        return contains(ImVector.centerOf(block));
    }

    default boolean contains(BlockState block) {
        return contains(ImVector.centerOf(block));
    }

    default boolean contains(Location point) {
        return contains(point.position());
    }

    default boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }

    /**
     * Test if moving from the first point to the second crosses into the region
     */
    default boolean enters(BlockVector from, BlockVector to) {
        return !contains(from) && contains(to);
    }

    default boolean enters(Optional<BlockVector> from, BlockVector to) {
        return from.isPresent() ? enters(from.get(), to)
                                : contains(to);
    }

    /**
     * Test if moving from the first point to the second crosses out of the region
     */
    default boolean exits(BlockVector from, BlockVector to) {
        return contains(from) && !contains(to);
    }

    default boolean exits(Optional<BlockVector> from, BlockVector to) {
        return from.isPresent() ? exits(from.get(), to)
                                : !contains(to);
    }

    default boolean canGetRandom() {
        return false;
    }

    default Vector getRandom(Random random) {
        throw new UnsupportedOperationException("Cannot generate a random point in " + this.getClass().getSimpleName());
    }

    default boolean isBlockBounded() {
        return false;
    }

    default org.bukkit.region.BlockRegion blockRegion() {
        return getBounds().blockRegion().filter(this::contains);
    }

    @Deprecated
    default Iterator<BlockVector> getBlockVectorIterator() {
        return Iterators.transform(
            blockRegion().mutableIterator(),
            BlockVector::new
        );
    }

    @Deprecated
    default Iterable<BlockVector> getBlockVectors() {
        return this::getBlockVectorIterator;
    }

    @Deprecated
    default Stream<BlockVector> blockPositions() {
        return Streams.of(getBlockVectorIterator());
    }

    default Iterable<Block> getBlocks(World world) {
        return () -> Iterators.transform(
            blockRegion().mutableIterator(),
            world::getBlockAt
        );
    }

    default Stream<Block> blocks(World world) {
        return blockPositions().map(pos -> BlockUtils.blockAt(world, pos));
    }

    default long blockVolume() {
        return blockPositions().count();
    }

    default Stream<ChunkPosition> chunkPositions() {
        final Cuboid bounds = getBounds();
        if(!bounds.isBlockFinite()) {
            throw new UnsupportedOperationException("Cannot enumerate chunks in unbounded region type " + getClass().getSimpleName());
        }

        final ChunkPosition
            min = ChunkPosition.ofBlock(bounds.minimumBlockInside()),
            max = ChunkPosition.ofBlock(bounds.maximumBlockInside());
        return IntStream.rangeClosed(min.x(), max.x())
                        .mapToObj(x -> IntStream.rangeClosed(min.z(), max.z())
                                                .mapToObj(z -> new ChunkPosition(x, z)))
                        .flatMap(Function.identity());
    }

    default Stream<BlockState> tileEntities(World world) {
        return chunkPositions().flatMap(cp -> Stream.of(cp.getChunk(world).getTileEntities()))
                               .filter(this::contains);
    }

    default Stream<Entity> entities(World world) {
        return chunkPositions().flatMap(cp -> Stream.of(cp.getChunk(world).getEntities()))
                               .filter(this::contains);
    }

    abstract class Impl extends FeatureDefinition.Impl implements Region {}
}
