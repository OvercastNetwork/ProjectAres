package tc.oc.pgm.events;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Subclass of {@link PlayerJoinPartyEvent} called in cases where
 * the player is joining the match i.e. {@link #getOldParty()} is null.
 *
 * This event is called at the end of the joining process, after the player
 * has joined the initial {@link Party}. That party cannot be changed from
 * within this event, nor can another party change be executed.
 *
 * A player's initial party can be changed from {@link MatchPlayerAddEvent}.
 *
 * @deprecated just use {@link PlayerChangePartyEvent} for everything
 */
@Deprecated
public class PlayerJoinMatchEvent extends PlayerJoinPartyEvent {

    public PlayerJoinMatchEvent(MatchPlayer player, Party newParty) {
        super(player, null, checkNotNull(newParty));
    }
}
