package tc.oc.pgm.filters.matcher.match;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import tc.oc.commons.core.util.EnumSets;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.match.MatchState;

public class MatchStateFilter extends TypedFilter.Impl<IMatchQuery> {

    private final @Inspect EnumSet<MatchState> states;

    public MatchStateFilter(MatchState... states) {
        this(Arrays.asList(states));
    }

    public MatchStateFilter(Collection<MatchState> states) {
        this.states = EnumSets.copySet(MatchState.class, states);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IMatchQuery query) {
        return states.contains(query.matchState());
    }

    private static final MatchStateFilter STARTED = new MatchStateFilter(MatchState.Running, MatchState.Finished);
    private static final MatchStateFilter RUNNING = new MatchStateFilter(MatchState.Running);
    private static final MatchStateFilter FINISHED = new MatchStateFilter(MatchState.Finished);

    public static Filter started() { return STARTED; }
    public static Filter running() { return RUNNING; }
    public static Filter finished() { return FINISHED; }
}
