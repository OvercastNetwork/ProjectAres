package tc.oc.pgm.filters.parser;

import com.google.common.cache.LoadingCache;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterTypeException;
import tc.oc.pgm.filters.query.IQuery;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.validate.Validation;

public class RespondsToQueryValidation implements Validation<Filter> {

    private static final LoadingCache<Class<? extends IQuery>, RespondsToQueryValidation> CACHE = CacheUtils.newCache(RespondsToQueryValidation::new);
    public static RespondsToQueryValidation get(Class<? extends IQuery> queryType) { return CACHE.getUnchecked(queryType); }

    private final Class<? extends IQuery> queryType;

    protected RespondsToQueryValidation(Class<? extends IQuery> queryType) {
        this.queryType = queryType;
    }

    @Override
    public void validate(Filter filter, Node node) throws InvalidXMLException {
        try {
            filter.assertRespondsTo(queryType);
        } catch(FilterTypeException e) {
            throw new InvalidXMLException(e.getMessage(), node);
        }
    }
}
