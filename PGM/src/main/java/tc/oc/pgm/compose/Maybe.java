package tc.oc.pgm.compose;

import java.util.stream.Stream;

import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.ITransientQuery;

public class Maybe<T> extends CompositionImpl<T> {

    @Inspect private final Filter filter;
    @Inspect private final Composition<T> element;

    public Maybe(Filter filter, Composition<T> element) {
        this.filter = filter;
        this.element = element;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Stream<T> dependencies() {
        return element.dependencies();
    }

    @Override
    public Stream<T> elements(ITransientQuery query) {
        return filter.query(query).isAllowed() ? element.elements(query)
                                               : Stream.empty();
    }
}
