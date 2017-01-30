package net.anxuiz.tourney;

import java.util.HashSet;
import javax.inject.Inject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import net.anxuiz.tourney.event.PartyReadyStatusChangeEvent;
import org.bukkit.Bukkit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.match.inject.MatchScoped;

@MatchScoped
public class ReadyManager {

    private final Match match;
    private final TeamManager teamManager;
    private final HashSet<Party> readyParties = new HashSet<>();

    @Inject ReadyManager(Match match, TeamManager teamManager) {
        this.match = match;
        this.teamManager = teamManager;
    }

    public void remove(Party party) {
        readyParties.remove(party);
    }

    /**
     * Determines whether or not the specified {@link Party} is ready.
     *
     * @param team The team to check.
     * @return Whether or not the team is ready.
     */
    public boolean isReady(Party team) {
        return this.readyParties.contains(team);
    }

    /**
     * Marks the specified {@link Party} as ready.
     *
     * @param team The team to mark.
     */
    public void markReady(Party team) {
        if (readyParties.contains(Preconditions.checkNotNull(team, "Team"))) return;
        this.readyParties.add(team);
        Bukkit.getPluginManager().callEvent(new PartyReadyStatusChangeEvent(false, team));
    }

    /**
     * Marks the specified {@link Party} as not ready.
     *
     * @param team The team to mark.
     */
    public void markNotReady(Party team) {
        if (!readyParties.contains(Preconditions.checkNotNull(team, "Team"))) return;
        this.readyParties.remove(team);
        Bukkit.getPluginManager().callEvent(new PartyReadyStatusChangeEvent(true, team));
    }

    /**
     * Determines whether or not the current match is ready to begin.
     *
     * @return Whether or not the current match is ready to begin.
     */
    public boolean readyToStart() {
        return isReady(match.getDefaultParty()) &&
               Iterables.all(teamManager.getMappedTeams(),
                             ReadyManager.this::isReady);
    }
}
