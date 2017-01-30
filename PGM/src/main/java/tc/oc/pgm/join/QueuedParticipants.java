package tc.oc.pgm.join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.match.ObservingParty;

/**
 * Observing party that holds players who have requested to join before
 * match start, when the server is configured to defer joins.
 * After the match starts, this party is empty.
 */
@MatchScoped
public class QueuedParticipants extends ObservingParty {

    private final JoinConfiguration config;
    private List<MatchPlayer> shuffledPlayers;

    @Inject QueuedParticipants(Match match, JoinConfiguration config) {
        super(match);
        this.config = config;
    }

    private void invalidateShuffle() {
        shuffledPlayers = null;
    }

    @Override
    public boolean addPlayerInternal(MatchPlayer player) {
        if(super.addPlayerInternal(player)) {
            invalidateShuffle();
            return true;
        }
        return false;
    }

    @Override
    public boolean removePlayerInternal(MatchPlayer player) {
        if(super.removePlayerInternal(player)) {
            invalidateShuffle();
            return true;
        }
        return false;
    }

    public List<MatchPlayer> getOrderedPlayers() {
        if(shuffledPlayers == null) {
            shuffledPlayers = new ArrayList<>(getPlayers());
            Collections.shuffle(shuffledPlayers);

            if(config.priorityKick()) {
                // If priority kicking is enabled, might as well join the high
                // priority players first so nobody actually gets kicked.
                final JoinMatchModule jmm = match.needMatchModule(JoinMatchModule.class);
                Collections.sort(shuffledPlayers, (a, b) ->
                    Boolean.compare(jmm.canPriorityKick(b), jmm.canPriorityKick(a))
                );
            }
        }
        return shuffledPlayers;
    }

    @Override
    public String getDefaultName() {
        return "Participants";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }
}
