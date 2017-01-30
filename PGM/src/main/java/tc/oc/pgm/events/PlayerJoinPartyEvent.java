package tc.oc.pgm.events;

import javax.annotation.Nullable;

import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Subclass of {@link PlayerPartyChangeEvent} called in cases where the player
 * is actually joining a party i.e. {@link #getNewParty()} returns non-null.
 *
 * @deprecated just use {@link PlayerChangePartyEvent} for everything
 */
@Deprecated
public class PlayerJoinPartyEvent extends PlayerPartyChangeEvent {
    public PlayerJoinPartyEvent(MatchPlayer player, @Nullable Party oldParty, Party newParty) {
        super(player, oldParty, checkNotNull(newParty));
    }

    /**
     * Overridden to remove @Nullable
     */
    @Override
    public Party getNewParty() {
        return super.getNewParty();
    }
}
