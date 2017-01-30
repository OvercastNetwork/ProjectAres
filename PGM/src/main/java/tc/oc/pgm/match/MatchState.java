package tc.oc.pgm.match;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/** Finite state machine for Match instances. */
public enum MatchState {
    /** Unstarted, not counting down */
    Idle,

    /** Counting down to start */
    Starting,

    /** Team huddle countdown */
    Huddle,

    /** Started */
    Running,

    /** Started and ended */
    Finished;

    public final tc.oc.api.docs.MatchState apiValue;

    MatchState() {
        apiValue = tc.oc.api.docs.MatchState.valueOf(name().toUpperCase());
    }

    /**
     * Can a match transition from this state to the given state?
     */
    public boolean canTransitionTo(MatchState newState) {
        return transitions.containsEntry(this, newState);
    }

    private static final SetMultimap<MatchState, MatchState> transitions = ImmutableSetMultimap.<MatchState, MatchState>builder()
        .putAll(Idle,           Starting, Huddle, Running)
        .putAll(Starting,       Idle, Huddle, Running)
        .putAll(Huddle,         Idle, Starting, Running)
        .putAll(Running,        Finished)
        .build();
}
