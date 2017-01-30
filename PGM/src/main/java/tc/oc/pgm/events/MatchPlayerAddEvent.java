package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;
import tc.oc.api.docs.PlayerId;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

/**
 * Called just after a {@link MatchPlayer} is constructed and initialized. The player has not
 * yet joined a {@link Party}, but their internal state is set as if they were already in
 * the default party.
 *
 * This event can be used to change the player's initial party. If that hasn't happened by the
 * time this event returns, the player will join the default party properly.
 *
 * If possible, {@link PlayerJoinMatchEvent} should be used instead of this event, which fires
 * as part of the process of joining the initial party.
 */
public class MatchPlayerAddEvent extends MatchEvent {

    private final MatchPlayer player;
    private final PlayerId playerId;

    public MatchPlayerAddEvent(Match match, MatchPlayer player) {
        super(match);
        this.player = player;
        this.playerId = player.getPlayerId();
    }

    public MatchPlayer getPlayer() {
        return player;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    private static HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
