package tc.oc.pgm.filters;

import java.util.List;

import com.google.common.collect.ImmutableList;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Party;

public final class Filterables {
    private Filterables() {}

    // Filterables ordered from general to specific
    public static final List<Class<? extends Filterable<?>>> SCOPES = ImmutableList.of(
        Match.class,
        Party.class,
        MatchPlayer.class
    );

    /**
     * Return the "scope" of the given filter, which is the most general
     * {@link Filterable} type that it responds to.
     */
    public static Class<? extends Filterable<?>> scope(Filter filter) {
        for(Class<? extends Filterable<?>> scope : SCOPES) {
            if(filter.respondsTo(scope)) return scope;
        }

        throw new IllegalStateException("Filter type " + filter.getDefinitionType().getSimpleName() +
                                        " does not have a filterable scope");
    }
}
