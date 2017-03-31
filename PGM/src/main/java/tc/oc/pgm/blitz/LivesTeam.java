package tc.oc.pgm.blitz;

import tc.oc.api.docs.PlayerId;
import tc.oc.pgm.match.Competitor;

public class LivesTeam extends LivesBase {

    public LivesTeam(Competitor competitor, int lives) {
        super(lives, competitor);
    }

    @Override
    public Type type() {
        return Type.TEAM;
    }

    @Override
    public boolean applicableTo(PlayerId player) {
        return competitor().players().anyMatch(matchPlayer -> matchPlayer.getPlayerId().equals(player));
    }

    @Override
    public boolean owner(PlayerId playerId) {
        return false;
    }

    @Override
    public int hashCode() {
        return competitor().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
               obj instanceof LivesTeam &&
               competitor().equals(((LivesTeam) obj).competitor());
    }

}
