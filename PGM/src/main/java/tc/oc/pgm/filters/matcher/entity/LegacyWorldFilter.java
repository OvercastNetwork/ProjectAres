package tc.oc.pgm.filters.matcher.entity;

import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.query.IEntityTypeQuery;
import tc.oc.pgm.filters.query.IQuery;

/**
 * Used to implement the legacy "allow-world" and "deny-world" filters
 */
public class LegacyWorldFilter extends TypedFilter.Impl<IQuery> {
    @Override
    public boolean matches(IQuery query) {
        return !(query instanceof IEntityTypeQuery);
    }
}
