package tc.oc.pgm.events;

import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Called immediately before a player joins a {@link Competitor}.
 * This differs from {@link PlayerPartyChangeEvent} in a few ways:
 *
 *  - It is only called when joining a party, and only when that party is participating
 *  - It is called before the change
 *
 *  TODO: Possibly remove this event, since it's not used anywhere. This event used to
 *  be {@link org.bukkit.event.Cancellable}, and it was used to deny joining e.g. in the
 *  middle of a Blitz match, but we do that with {@link tc.oc.pgm.join.JoinHandler}s now.
 */
public class PlayerParticipationStartEvent extends PlayerParticipationEvent {
    public PlayerParticipationStartEvent(MatchPlayer player, Competitor competitor) {
        super(player, competitor);
    }
}
