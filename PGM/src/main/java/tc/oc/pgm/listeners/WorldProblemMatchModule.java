package tc.oc.pgm.listeners;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.api.util.Permissions;
import tc.oc.commons.bukkit.util.BlockVectorSet;
import tc.oc.commons.bukkit.util.ChunkPosition;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.LOADED)
public class WorldProblemMatchModule extends MatchModule implements Listener {

    private static final int RANDOM_TICK_SPEED_LIMIT = 30;

    private final Set<ChunkPosition> repairedChunks = new HashSet<>();
    private final BlockVectorSet block36Locations = new BlockVectorSet();
    
    @Inject private @MapProto SemanticVersion proto;
    @Inject private World world;

    @Inject WorldProblemMatchModule(Match match) {
        super(match);
    }

    void broadcastDeveloperWarning(String message) {
        logger.warning(message);
        Bukkit.broadcast(ChatColor.RED + message, Permissions.MAPERRORS);
    }

    @Override
    public void load() {
        super.load();

        final String str = world.getGameRuleValue("randomTickSpeed");
        if(str != null) {
            try {
                int value = Integer.parseInt(str);
                if(value > RANDOM_TICK_SPEED_LIMIT) {
                    broadcastDeveloperWarning("Gamerule 'randomTickSpeed' is set to " + value + " for this world (normal value is 3). This may overload the server.");
                }
            } catch(NumberFormatException ignored) {}
        }

        for(Chunk chunk : world.getLoadedChunks()) {
            checkChunk(chunk);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if(world.equals(event.getWorld())) {
            checkChunk(event.getChunk());
        }
    }

    private void checkChunk(Chunk chunk) {
        checkChunk(ChunkPosition.of(chunk), chunk);
    }

    private void checkChunk(ChunkPosition pos, @Nullable Chunk chunk) {
        if(repairedChunks.add(pos)) {
            if(chunk == null) {
                chunk = pos.getChunk(match.getWorld());
            }

            for(BlockState state : chunk.getTileEntities()) {
                if(state instanceof Skull) {
                    if(!NMSHacks.isSkullCached((Skull) state)) {
                        Location loc = state.getLocation();
                        broadcastDeveloperWarning("Uncached skull \"" + ((Skull) state).getOwner() + "\" at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                    }
                }
            }

            // Replace formerly invisible half-iron-door blocks with barriers
            for(Block ironDoor : chunk.getBlocks(Material.IRON_DOOR_BLOCK)) {
                BlockFace half = (ironDoor.getData() & 8) == 0 ? BlockFace.DOWN : BlockFace.UP;
                if(ironDoor.getRelative(half.getOppositeFace()).getType() != Material.IRON_DOOR_BLOCK) {
                    ironDoor.setType(Material.BARRIER, false);
                }
            }
            if (proto.isOlderThan(ProtoVersions.ENABLE_BLOCK_36)) {
                // Remove all block 36 and remember the ones at y=0 so VoidFilter can check them
                for(Block block36 : chunk.getBlocks(Material.PISTON_MOVING_PIECE)) {
                    if(block36.getY() == 0) {
                        block36Locations.add(block36.getX(), block36.getY(), block36.getZ());
                    }
                    block36.setType(Material.AIR, false);
                }
            }
        }
    }

    public boolean wasBlock36(int x, int y, int z) {
        checkChunk(ChunkPosition.ofBlock(x, y, z), null);
        return block36Locations.contains(x, y, z);
    }
}
