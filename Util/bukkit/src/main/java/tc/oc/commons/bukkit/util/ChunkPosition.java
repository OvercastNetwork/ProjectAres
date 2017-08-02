package tc.oc.commons.bukkit.util;

import java.util.Objects;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.geometry.Vec3;
import tc.oc.commons.core.util.Utils;

public class ChunkPosition {

    final int x, z;

    public ChunkPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    public Chunk getChunk(World world) {
        return world.getChunkAt(x, z);
    }

    @Override
    public final boolean equals(Object obj) {
        return Utils.equals(ChunkPosition.class, this, obj, that ->
            this.x() == that.x() &&
            this.z() == that.z()
        );
    }

    @Override
    public final int hashCode() {
        return Objects.hash(x(), z());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + x + ", " + z + ")";
    }

    public static ChunkPosition of(Chunk chunk) {
        return new ChunkPosition(chunk.getX(), chunk.getZ());
    }

    public static ChunkPosition ofBlock(int x, int y, int z) {
        return new ChunkPosition(x >> 4, z >> 4);
    }

    public static ChunkPosition ofBlock(Vec3 pos) {
        return ofBlock(pos.coarseX(), pos.coarseY(), pos.coarseZ());
    }

    public static ChunkPosition ofBlock(Location loc) {
        return ofBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static ChunkPosition ofBlock(Block block) {
        return ofBlock(block.getX(), block.getY(), block.getZ());
    }
}
