package tc.oc.pgm.playerstats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.core.Core;
import tc.oc.pgm.core.CoreBlockBreakEvent;
import tc.oc.pgm.core.CoreLeakEvent;
import tc.oc.pgm.destroyable.Destroyable;
import tc.oc.pgm.destroyable.DestroyableContribution;
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.MatchScoreChangeEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.flag.event.FlagCaptureEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchUserFacet;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.inject.ForMatchUser;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;
import tc.oc.pgm.wool.PlayerWoolPlaceEvent;

import javax.inject.Inject;
import java.util.*;

public class StatsUserFacet implements MatchUserFacet, Listener {

    private final UUID player;
    private int lifeKills, teamKills, matchKills, deaths;
    private List<Long> woolCaptureTimes;
    private Map<Core, Long> coreTouchTimes;
    private List<Long> coreLeakTimes;
    private List<Long> flagCaptureTimes;
    private double pointsScored;
    private HashMap<DestroyableContribution, Long> destroyableDestroyTimes;
    private int blocksBroken;

    @Inject public StatsUserFacet(@ForMatchUser UUID player) {
        this.player = player;
        woolCaptureTimes = new ArrayList<>();
        coreTouchTimes = new HashMap<>();
        coreLeakTimes = new ArrayList<>();
        flagCaptureTimes = new ArrayList<>();
        pointsScored = 0;
        destroyableDestroyTimes = new HashMap<>();
        blocksBroken = 0;
    }

    /**
     * Get the amount of kills since this player last spawned.
     */
    public int lifeKills() {
        return lifeKills;
    }

    /**
     * Get the amount of kills since this player has been on a party.
     */
    public int teamKills() {
        return teamKills;
    }

    /**
     * Get the amount of kills this player got in the whole match.
     */
    public int matchKills() {
        return matchKills;
    }

    /**
     * Amount of times this player died.
     */
    public int deaths() {
        return deaths;
    }

    public List<Long> getWoolCaptureTimes() { return woolCaptureTimes; }

    public Map<Core, Long> getCoreTouchTimes() {
        return coreTouchTimes;
    }

    public List<Long> getCoreLeakTimes() {
        return coreLeakTimes;
    }

    public List<Long> getFlagCaptureTimes() {
        return flagCaptureTimes;
    }

    public double getPointsScored() {
        return pointsScored;
    }

    public HashMap<DestroyableContribution, Long> getDestroyableDestroyTimes() {
        return destroyableDestroyTimes;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(final MatchPlayerDeathEvent event) {
        ParticipantState killer = event.getKiller();
        if (event.getVictim().getUniqueId().equals(player)) {
            ++deaths;
        } else if (killer != null && killer.getUniqueId().equals(player) && event.isEnemyKill()) {
            ++lifeKills;
            ++teamKills;
            ++matchKills;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWoolPlace(final PlayerWoolPlaceEvent event) {
        if (event.getWool().isVisible() && event.getPlayer().getUniqueId().equals(player)) {
            woolCaptureTimes.add(event.getMatch().runningTime().toMinutes());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagCapture(final FlagCaptureEvent event) {
        if (event.getGoal().isVisible() && event.getCarrier().getUniqueId().equals(player)) {
            flagCaptureTimes.add(event.getMatch().runningTime().toMinutes());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroyableDestroyed(final DestroyableDestroyedEvent event) {
        Destroyable destroyable = event.getDestroyable();

        if (destroyable.isVisible()) {
            for (DestroyableContribution entry : event.getDestroyable().getContributions()) {
                if (entry.getPlayerState().getUniqueId().equals(player)) {
                    destroyableDestroyTimes.put(entry, event.getMatch().runningTime().toMinutes());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCoreBlockBreak(final CoreBlockBreakEvent event) {
        if (event.getCore().isVisible() && event.getPlayer().getUniqueId().equals(player)) {
            coreTouchTimes.put(event.getCore(), event.getMatch().runningTime().toMinutes());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCoreLeak(final CoreLeakEvent event) {
        if (event.getCore().isVisible() && coreTouchTimes.containsKey(event.getCore())) {
            coreLeakTimes.add(coreTouchTimes.get(event.getCore()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScoreEvent(final MatchScoreChangeEvent event) {
        Optional<MatchPlayer> matchPlayerOptional = event.getPlayer();
        if (matchPlayerOptional != null) {
            if (matchPlayerOptional.isPresent() && matchPlayerOptional.get().getUniqueId().equals(player)) {
                pointsScored += event.getNewScore() - event.getOldScore();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onScoreEvent(final BlockBreakEvent event) {
        if (event.getPlayer().getUniqueId().equals(player)) {
            ++blocksBroken;
        }
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(final ParticipantSpawnEvent event) {
        lifeKills = 0;
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyChange(final PlayerPartyChangeEvent event) {
        lifeKills = 0;
        teamKills = 0;
        woolCaptureTimes.clear();
        coreTouchTimes.clear();
        coreLeakTimes.clear();
        flagCaptureTimes.clear();
        pointsScored = 0;
        destroyableDestroyTimes.clear();
        blocksBroken = 0;
    }
}
