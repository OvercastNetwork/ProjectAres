package tc.oc.pgm.flag.state;

import org.bukkit.Location;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.Party;
import tc.oc.pgm.flag.Post;
import tc.oc.pgm.teams.Team;

import javax.annotation.Nullable;

public interface State {

    boolean isCurrent();

    boolean isCarrying(MatchPlayer player);

    boolean isCarrying(ParticipantState player);

    boolean isCarrying(Party team);

    boolean isAtPost(Post post);

    Post getPost();

    @Nullable Team getController();

    Iterable<Location> getProximityLocations(ParticipantState player);
}
