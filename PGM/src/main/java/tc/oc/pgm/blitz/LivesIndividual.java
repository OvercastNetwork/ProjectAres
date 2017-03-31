package tc.oc.pgm.blitz;

import tc.oc.api.docs.PlayerId;
import tc.oc.pgm.match.MatchPlayer;

public class LivesIndividual extends LivesBase {

    private final PlayerId player;

    public LivesIndividual(MatchPlayer player, int lives) {
        super(lives, player.getCompetitor());
        this.player = player.getPlayerId();
    }

    public PlayerId player() {
        return player;
    }

    @Override
    public Type type() {
        return Type.INDIVIDUAL;
    }

    @Override
    public boolean applicableTo(PlayerId player) {
        return player().equals(player);
    }

    @Override
    public boolean owner(PlayerId playerId) {
        return player().equals(playerId);
    }

    @Override
    public int hashCode() {
        return player().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
               obj instanceof LivesIndividual &&
               player().equals(((LivesIndividual) obj).player());
    }

}
