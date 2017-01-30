package net.anxuiz.tourney;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import tc.oc.api.docs.Entrant;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.Match;

/** Represents a best-two-out-of-three KotH or TDM session. */
public class KDMSession {

    private final Tourney tourney;
    private final PGMMap map;

    private final HashMap<String, Entrant> winnerStore = new HashMap<>(3);
    private final Set<Entrant> entrants;

    @Inject KDMSession(TeamManager teamManager, Tourney tourney, PGMMap map) {
        this.tourney = tourney;
        this.map = map;
        this.entrants = teamManager.getEntrants();
    }

    public Set<Entrant> getEntrants() {
        return entrants;
    }

    public PGMMap getMap() {
        return this.map;
    }

    /**
     * Attempts to calculate the winner, returning either the winning {@link Entrant},
     * or <code>null</code>, if the winner can not yet be determined. Optionally records the winning match.
     *
     * @return The winning {@link Entrant}, or <code>null</code>, if the winner
     * can not yet be determined.
     */
    public @Nullable Entrant calculateWinner() {
        HashMap<Entrant, Integer> wins = new HashMap<>();
        for (Map.Entry<String, Entrant> match : this.winnerStore.entrySet()) {
            Entrant winner = match.getValue();
            if (winner != null) {
                int winCount = Optional.fromNullable(wins.get(winner)).or(0) + 1;
                wins.put(winner, winCount);
                if (winCount > 1) {
                    tourney.recordMatch(match.getKey());
                    return winner;
                }
            }
        }

        if (this.getMatchesPlayed() > 2) tourney.recordMatch(this.winnerStore.keySet().iterator().next());
        return null;
    }

    public int getMatchesPlayed() {
        return this.winnerStore.size();
    }

    public void appendMatch(Match match, Entrant winner) {
        if (!this.winnerStore.containsKey(Preconditions.checkNotNull(match.getId(), "Match"))) {
            this.winnerStore.put(match.getId(), winner);
        }
    }
}
