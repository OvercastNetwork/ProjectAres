package tc.oc.commons.bukkit.util;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Physical;
import org.bukkit.World;
import org.bukkit.block.Block;
import tc.oc.commons.core.util.Utils;

public class ChunkLocation implements Physical {

    private final UUID worldId;
    private final ChunkPosition position;

    public ChunkLocation(UUID worldId, ChunkPosition position) {
        this.worldId = worldId;
        this.position = position;
    }

    public UUID worldId() {
        return worldId;
    }

    public ChunkPosition position() {
        return position;
    }

    @Override
    public World getWorld() {
        return Bukkit.world(worldId);
    }

    public Chunk getChunk() {
        return position.getChunk(getWorld());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(worldId(), position());
    }

    @Override
    public final boolean equals(Object obj) {
        return Utils.equals(ChunkLocation.class, this, obj, that ->
            this.worldId().equals(that.worldId()) &&
            this.position().equals(that.position())
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{world=" + worldId() +
               " position=(" + position().x() + ", " + position().z() + ")}";
    }

    public static ChunkLocation of(World world, ChunkPosition position) {
        return new ChunkLocation(world.getUID(), position);
    }

    public static ChunkLocation of(Chunk chunk) {
        return of(chunk.getWorld(), ChunkPosition.of(chunk));
    }

    public static ChunkLocation ofBlock(Location loc) {
        return new ChunkLocation(loc.getWorldId(), ChunkPosition.ofBlock(loc));
    }

    public static ChunkLocation ofBlock(Block block) {
        return ofBlock(block.getLocation());
    }
}
