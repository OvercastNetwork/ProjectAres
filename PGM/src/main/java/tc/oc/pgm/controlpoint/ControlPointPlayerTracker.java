package tc.oc.pgm.controlpoint;

import com.google.common.collect.Sets;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.commons.bukkit.event.CoarsePlayerMoveEvent;
import tc.oc.pgm.regions.Region;
import tc.oc.commons.core.util.DefaultMapAdapter;
import tc.oc.pgm.utils.MatchPlayers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tracks which players are on a control point and answers some queries about them
 */
@ListenerScope(MatchScope.LOADED)
public class ControlPointPlayerTracker implements Listener {
    protected final Match match;
    protected final Region captureRegion;
    protected final Set<MatchPlayer> playersOnPoint = Sets.newHashSet();

    public ControlPointPlayerTracker(Match match, Region captureRegion) {
        this.match = match;
        this.captureRegion = captureRegion;
    }

    public Set<MatchPlayer> getPlayersOnPoint() {
        return this.playersOnPoint;
    }

    /**
     * Get the number of players that each team in the match has on the point
     */
    public Map<Competitor, Integer> getPlayerCountsByTeam() {
        // calculate how many players from each team are on the hill
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
    public void onPlayerMove(final CoarsePlayerMoveEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        this.handlePlayerMove(event.getPlayer(), event.getTo().toVector());
    }

    private void handlePlayerMove(Player bukkit, Vector to) {
        MatchPlayer player = this.match.getPlayer(bukkit);
        if(!MatchPlayers.canInteract(player)) return;

        if(!player.getBukkit().isDead() && this.captureRegion.contains(to.toBlockVector())) {
            this.playersOnPoint.add(player);
        } else {
            this.playersOnPoint.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDespawn(final ParticipantDespawnEvent event) {
        playersOnPoint.remove(event.getPlayer());
    }
}
