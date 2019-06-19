package tc.oc.pgm.payload;

import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.utils.MatchPlayers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks which players are by a payload and answers some queries about them
 */
@ListenerScope(MatchScope.LOADED)
public class PayloadPlayerTracker implements Listener {
    protected final Match match;
    protected Location location;
    protected final double radius;
    protected final double height;
    protected final Minecart payload;
    protected final Set<MatchPlayer> playersOnPoint = Sets.newHashSet();

    public PayloadPlayerTracker(Match match, Location location, double radius, double height, Minecart payload) {
        this.match = match;
        this.location = location;
        this.radius = radius;
        this.height = height;
        this.payload = payload;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Set<MatchPlayer> getPlayersOnPoint() {
        return this.playersOnPoint;
    }

    /**
     * Get the number of players that each team in the match has on the point
     */
    public Map<Competitor, Integer> getPlayerCountsByTeam() {
        // calculate how many players from each team are by the payload
        Map<Competitor, Integer> counts = new DefaultMapAdapter<>(new HashMap<>(), 0);
        for(MatchPlayer player : this.getPlayersOnPoint()) {
            Competitor team = player.getCompetitor();
            counts.put(team, counts.get(team) + 1);
        }
        return counts;
    }

    /**
     * Get the number of players that each team in the match has on the point, sorted from most to least
     */
    public List<Map.Entry<Competitor, Integer>> getSortedPlayerCountsByTeam() {
        // reverse natural ordering of value
        return new ArrayList<>(this.getPlayerCountsByTeam().entrySet()).stream().sorted((o1, o2) -> Integer.compare(o2.getValue(), o1.getValue())).collect(Collectors.toList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector());
    }

    private void handlePlayerMove(Player bukkit, Vector to) {
        MatchPlayer player = this.match.getPlayer(bukkit);
        if(!MatchPlayers.canInteract(player)) return;

        if(isOnPoint(player, to)) { //Determine if they are in the height
            this.playersOnPoint.add(player);
        } else {
            this.playersOnPoint.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDespawn(final ParticipantDespawnEvent event) {
        playersOnPoint.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDespawn(final PlayerInteractEntityEvent event) {
        if (event.getRightClicked().equals(payload)) {
            event.setCancelled(true);
        }
    }

    public void removePlayerOnPoint(MatchPlayer player) {
        this.playersOnPoint.remove(player);
    }

    public boolean isOnPoint(MatchPlayer player, Vector location) {
        Vector payloadLocation = this.location.toVector();
        return !player.getBukkit().isDead() &&
                Math.sqrt(Math.pow(location.getX() - payloadLocation.getX(), 2) + Math.pow(location.getZ() - payloadLocation.getBlockZ(), 2)) <= this.radius && //Determine if they are in radius
                Math.abs(location.getY() - payloadLocation.getY()) <= this.height;
    }

    public boolean hasPlayersOnPoint(Competitor competitor) {
        if (competitor == null) {
            return false;
        }

        for (MatchPlayer player : playersOnPoint) {
            if (player.getCompetitor() != null && player.getCompetitor().equals(competitor)) {
                return true;
            }
        }
        return false;
    }
}
