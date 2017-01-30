package tc.oc.pgm.match;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.EntityLocation;
import tc.oc.commons.bukkit.nick.Identity;

/**
 * A {@link MatchPlayerState} that can only represent a {@link Competitor},
 * and records the player's location as part of the state.
 */
public class ParticipantState extends MatchPlayerState {

    public ParticipantState(Match match, Identity player, UUID uuid, Competitor competitor, EntityLocation location) {
        super(match, player, uuid, competitor, location);
    }

    @Override
    public Optional<ParticipantState> participantState() {
        return Optional.of(this);
    }

    @Override
    public Competitor getParty() {
        return (Competitor) super.getParty();
    }
}
