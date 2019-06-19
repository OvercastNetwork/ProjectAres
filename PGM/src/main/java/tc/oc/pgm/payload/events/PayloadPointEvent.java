package tc.oc.pgm.payload.events;

import tc.oc.pgm.events.MatchEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.payload.Payload;

public abstract class PayloadPointEvent extends MatchEvent {
    protected final Payload payload;

    public PayloadPointEvent(Match match, Payload payload) {
        super(match);
        this.payload = payload;
    }

    public Payload getPayload() {
        return this.payload;
    }
}
