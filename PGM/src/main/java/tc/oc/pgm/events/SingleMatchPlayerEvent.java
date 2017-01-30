package tc.oc.pgm.events;

import java.util.stream.Stream;

import tc.oc.pgm.match.MatchPlayer;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class SingleMatchPlayerEvent extends MatchEvent implements MatchPlayerEvent {

    protected final MatchPlayer player;

    public SingleMatchPlayerEvent(MatchPlayer player) {
        super(player.getMatch());
        this.player = checkNotNull(player);
    }

    /** Gets the player who joined the match. */
    public MatchPlayer getPlayer() {
        return this.player;
    }

    @Override
    public Stream<MatchPlayer> players() {
        return Stream.of(getPlayer());
    }
}
