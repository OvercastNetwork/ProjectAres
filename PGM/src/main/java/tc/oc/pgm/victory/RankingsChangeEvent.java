package tc.oc.pgm.victory;

import java.util.Collection;

import org.bukkit.event.HandlerList;
import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;

public class RankingsChangeEvent extends MatchEvent {

    private final Collection<Competitor> before, after;

    public RankingsChangeEvent(Match match, Collection<Competitor> before, Collection<Competitor> after) {
        super(match);
        this.before = before;
        this.after = after;
    }

    public Collection<Competitor> before() {
        return before;
    }

    public Collection<Competitor> after() {
        return after;
    }

    private static final HandlerList handlers = new HandlerList();
}
