package tc.oc.pgm.filters.matcher;

import tc.oc.pgm.filters.query.IQuery;

public class QueryTypeFilter extends TypedFilter.Impl<IQuery> {
    protected final @Inspect Class<? extends IQuery> type;

    public QueryTypeFilter(Class<? extends IQuery> type) {
        this.type = type;
    }

    @Override
    public boolean matches(IQuery query) {
        return type.isInstance(query);
    }
}
