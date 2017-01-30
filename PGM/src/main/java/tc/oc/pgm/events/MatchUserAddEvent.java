package tc.oc.pgm.events;

import org.bukkit.event.HandlerList;
import tc.oc.api.docs.User;
import tc.oc.pgm.match.Match;

public class MatchUserAddEvent extends MatchEvent {

    private final User user;

    public MatchUserAddEvent(Match match, User user) {
        super(match);
        this.user = user;
    }

    public User user() {
        return user;
    }

    private static final HandlerList handlers = new HandlerList();
}
