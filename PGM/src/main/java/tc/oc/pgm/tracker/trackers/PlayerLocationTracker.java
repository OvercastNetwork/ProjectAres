package tc.oc.pgm.tracker.trackers;

import edu.umd.cs.findbugs.detect.BadUseOfReturnValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PlayerChangePartyEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

@ListenerScope(MatchScope.RUNNING)
public class PlayerLocationTracker implements Listener {

    private static final Map<MatchPlayer, Location> lastParticipatingLocation = new WeakHashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeParty(PlayerChangePartyEvent event) {
        lastParticipatingLocation.put(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerDeath(MatchPlayerDeathEvent event) {
        lastParticipatingLocation.put(event.getVictim(), event.getVictim().getLocation());
    }

    /*
    public static void setLocation(MatchPlayer player, Location location) {
        lastParticipatingLocation.put(player, location);
    }
    */

    public static @Nullable Location getLastParticipatingLocation(MatchPlayer player) {
        Location location = lastParticipatingLocation.get(player);
        return (location != null && location.getWorldId().equals(player.getWorldId())) ? location : null;
    }
}
