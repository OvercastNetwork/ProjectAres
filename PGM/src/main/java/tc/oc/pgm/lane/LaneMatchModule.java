package tc.oc.pgm.lane;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.ChatColor;
import org.bukkit.EntityLocation;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.commons.bukkit.util.Materials;
import tc.oc.commons.core.stream.BiStream;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.events.BlockTransformEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.PlayerBlockTransformEvent;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.Union;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamMatchModule;

@ListenerScope(MatchScope.RUNNING)
public class LaneMatchModule extends MatchModule implements Listener {
    private final Map<Team, Region> lanes;
    private final Set<PlayerId> voidPlayers = Sets.newHashSet();

    @Inject private LaneMatchModule(List<Lane> lanes, TeamMatchModule teams) {
        this.lanes = BiStream.fromValues(lanes.stream(), lane -> teams.team(lane.team()))
                             .mapValues(lane -> Union.of(lane.regions()))
                             .collect(Collectors.toImmutableMap());
    }

    @Override
    public void disable() {
        this.voidPlayers.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void checkLaneMovement(final CoarsePlayerMoveEvent event) {
        MatchPlayer player = this.match.getPlayer(event.getPlayer());
        if(player == null ||
           !player.canInteract() ||
           !(player.getParty() instanceof Team) ||
           player.getBukkit().getGameMode() == GameMode.CREATIVE ||
           event.getTo().getY() <= 0) return;

        Region laneRegion = this.lanes.get(player.getParty());
        if(laneRegion == null) return;

        boolean containsFrom = laneRegion.contains(event.getBlockFrom().toVector());
        boolean containsTo = laneRegion.contains(event.getBlockTo().toVector());

        // prevent ender pearling to the other lane
        if(!containsTo && event.getCause() instanceof PlayerTeleportEvent) {
            if(((PlayerTeleportEvent) event.getCause()).getCause() == TeleportCause.ENDER_PEARL) {
                event.setCancelled(true, new TranslatableComponent("match.lane.enderPearl.disabled"));
                return;
            }
        }

        if(this.voidPlayers.contains(player.getPlayerId())) {
            event.getPlayer().setFallDistance(0);
            // they have been marked as "out of lane"
            if(containsTo && !containsFrom) {
                // prevent the player from re-entering the lane
                event.setCancelled(true, new TranslatableComponent("match.lane.reEntry.disabled"));
            } else {
                // if they are going to land on something, teleport them underneith it
                Block under = event.getTo().clone().add(new Vector(0, -1, 0)).getBlock();
                if(under != null && under.getType() != Material.AIR) {
                    // teleport them to the lowest block
                    Vector safe = getSafeLocationUnder(under);
                    EntityLocation safeLocation = event.getPlayer().getEntityLocation();
                    safeLocation.setPosition(safe);
                    event.setTo(safeLocation);
                }
            }
        } else {
            if(!containsFrom && !containsTo) {
                // they are outside of the lane
                if(isIllegallyOutsideLane(laneRegion, event.getTo())) {
                    this.voidPlayers.add(player.getPlayerId());
                    event.getPlayer().sendMessage(ChatColor.RED + PGMTranslations.t("match.lane.exit", player));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void preventBlockPlaceByVoidPlayer(final BlockTransformEvent event) {
        if(event instanceof PlayerBlockTransformEvent) {
            event.setCancelled(this.voidPlayers.contains(((PlayerBlockTransformEvent) event).getPlayerState().getPlayerId()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void clearLaneStatus(final ParticipantDespawnEvent event) {
        this.voidPlayers.remove(event.getPlayer().getPlayerId());
    }

    private static BlockFace[] CARDINAL_DIRECTIONS = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static BlockFace[] DIAGONAL_DIRECTIONS = { BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST };

    private static Block getAdjacentRegionBlock(Region region, Block origin) {
        for(BlockFace face : ObjectArrays.concat(CARDINAL_DIRECTIONS, DIAGONAL_DIRECTIONS, BlockFace.class)) {
            Block adjacent = origin.getRelative(face);
            if(region.contains(BlockUtils.center(adjacent).toVector())) {
                return adjacent;
            }
        }
        return null;
    }

    private static boolean isIllegalBlock(Region region, Block block) {
        Block adjacent = getAdjacentRegionBlock(region, block);
        return adjacent == null || Materials.isColliding(adjacent.getType());
    }

    private static boolean isIllegallyOutsideLane(Region lane, Location loc) {
        Block feet = loc.getBlock();
        if(feet == null) return false;

        if(isIllegalBlock(lane, feet)) {
            return true;
        }

        Block head = feet.getRelative(BlockFace.UP);
        if(head == null) return false;

        if(isIllegalBlock(lane, head)) {
            return true;
        }

        return false;
    }

    private static Vector getSafeLocationUnder(Block block) {
        World world = block.getWorld();
        for(int y = block.getY() - 2; y >= 0; y--) {
            Block feet = world.getBlockAt(block.getX(), y, block.getZ());
            Block head = world.getBlockAt(block.getX(), y + 1, block.getZ());
            if(feet.getType() == Material.AIR && head.getType() == Material.AIR) {
                return new Vector(block.getX() + 0.5, y, block.getZ() + 0.5);
            }
        }
        return new Vector(block.getX() + 0.5, -2, block.getZ() + 0.5);
    }
}
