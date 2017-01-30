package tc.oc.pgm.match;

import java.util.Set;

import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.CompetitorDoc;

/**
 * Something that can exclusively win a {@link Match}
 */
public interface Competitor extends Party {

    /**
     * Return an ID that is constant and unique for the entire match
     */
    String getId();

    org.bukkit.scoreboard.Team.OptionStatus getNameTagVisibility();

    /**
     * All players who have ever been in this party after match commitment
     */
    Set<PlayerId> getPastPlayers();

    /**
     * Called when the match is committed (see {@link Match#commit()})
     */
    void commit();

    CompetitorDoc getDocument();
}
