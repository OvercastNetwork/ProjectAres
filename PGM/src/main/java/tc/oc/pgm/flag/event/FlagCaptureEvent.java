package tc.oc.pgm.flag.event;

import tc.oc.pgm.flag.Flag;
import tc.oc.pgm.flag.FlagDefinition;
import tc.oc.pgm.flag.Net;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.match.MatchPlayer;

import static com.google.common.base.Preconditions.checkNotNull;

public class FlagCaptureEvent extends GoalCompleteEvent {

    private final Net net;
    private final MatchPlayer carrier;
    private final boolean allFlagsCaptured;

    public FlagCaptureEvent(Flag flag, MatchPlayer carrier, Net net) {
        super(flag, true, c -> false, c -> c.equals(carrier.getParty()));

        this.net = checkNotNull(net);
        this.carrier = checkNotNull(carrier);

        boolean allFlagsCaptured = true;
        for(FlagDefinition def : this.net.getCapturableFlags()) {
            if(!def.getGoal(getMatch()).isCaptured()) allFlagsCaptured = false;
        }
        this.allFlagsCaptured = allFlagsCaptured;
    }

    @Override
    public Flag getGoal() {
        return (Flag) super.getGoal();
    }

    public Net getNet() {
        return net;
    }

    public MatchPlayer getCarrier() {
        return carrier;
    }

    /**
     * True if all the flags that can be captured in this net are currently in the
     * {@link tc.oc.pgm.flag.state.Captured} state, as of the moment the event was fired.
     * (they may not necessarily be in that state when the listener receives the event).
     */
    public boolean areAllFlagsCaptured() {
        return allFlagsCaptured;
    }
}
