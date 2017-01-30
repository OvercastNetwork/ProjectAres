package tc.oc.pgm.filters.matcher.player;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IPartyQuery;

public class ParticipatingFilter extends TypedFilter.Impl<IPartyQuery> {

    public static final Filter PARTICIPATING = new ParticipatingFilter(true);
    public static final Filter OBSERVING = new ParticipatingFilter(false);

    public static Filter of(boolean response) {
        return response ? PARTICIPATING : OBSERVING;
    }

    public ParticipatingFilter(boolean participating) {
        this.participating = participating;
    }

    private final @Inspect boolean participating;

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IPartyQuery query) {
        return query.isParticipating() == participating;
    }

    @Override
    public Filter not() {
        return of(!participating);
    }

    @Override
    public Filter and(Filter that) {
        if(that instanceof ParticipatingFilter) {
            return this.participating == ((ParticipatingFilter) that).participating
                   ? this : StaticFilter.DENY;
        }
        return super.and(that);
    }

    @Override
    public Filter or(Filter that) {
        if(that instanceof ParticipatingFilter) {
            return this.participating == ((ParticipatingFilter) that).participating
                   ? this : StaticFilter.ALLOW;
        }
        return super.and(that);
    }
}
