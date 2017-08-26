package tc.oc.pgm.filters.matcher.match;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;

public class MatchMutationFilter extends TypedFilter.Impl<IMatchQuery> {
    protected final @Inspect Mutation mutation;

    public MatchMutationFilter(Mutation mutation) {
        this.mutation = mutation;
    }

    @Override
    public boolean matches(IMatchQuery query) {
        return query.module(MutationMatchModule.class)
                    .filter(mmm -> mmm.enabled(mutation))
                    .isPresent();
    }
}
