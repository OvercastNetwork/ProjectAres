package tc.oc.pgm.core;

import tc.oc.pgm.match.Match;
import tc.oc.pgm.events.MatchEvent;

public abstract class CoreEvent extends MatchEvent {
    protected final Core core;

    public CoreEvent(Match match, Core core) {
        super(match);
        this.core = core;
    }

    public Core getCore() {
        return this.core;
    }
}
