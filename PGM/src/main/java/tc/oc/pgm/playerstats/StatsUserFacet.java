package tc.oc.pgm.playerstats;

import java.util.UUID;
import javax.inject.Inject;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PlayerPartyChangeEvent;
import tc.oc.pgm.match.MatchUserFacet;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.inject.ForMatchUser;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

public class StatsUserFacet implements MatchUserFacet, Listener {

    private final UUID player;
    private int lifeKills, teamKills, matchKills, deaths;

    @Inject public StatsUserFacet(@ForMatchUser UUID player) {
        this.player = player;
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

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(final ParticipantSpawnEvent event) {
        lifeKills = 0;
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPartyChange(final PlayerPartyChangeEvent event) {
        lifeKills = 0;
        teamKills = 0;
    }
}
