package tc.oc.pgm.filters;

import tc.oc.pgm.features.FeatureDefinitionException;
import tc.oc.pgm.filters.query.IQuery;

public class FilterTypeException extends FeatureDefinitionException {

    private final Class<? extends IQuery> queryType;

    public FilterTypeException(Filter filter, Class<? extends IQuery> queryType) {
        super("Filter type " + filter.getDefinitionType().getSimpleName() +
              " cannot respond to queries of type " + queryType.getSimpleName(), filter);
        this.queryType = queryType;
    }

    public Class<? extends IQuery> queryType() {
        return queryType;
    }
}
