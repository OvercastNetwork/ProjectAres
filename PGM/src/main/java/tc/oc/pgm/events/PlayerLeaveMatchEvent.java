package tc.oc.pgm.events;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

/**
 * Subclass of {@link PlayerLeavePartyEvent} called in cases where the player is leaving the match
 *
 * @deprecated just use {@link PlayerChangePartyEvent} for everything
 */
@Deprecated
public class PlayerLeaveMatchEvent extends PlayerLeavePartyEvent {
    public PlayerLeaveMatchEvent(MatchPlayer player, Party oldParty) {
        super(player, oldParty, null);
    }
}
