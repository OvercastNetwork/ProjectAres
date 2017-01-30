package tc.oc.pgm.compose;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.pgm.filters.query.ITransientQuery;

public class All<T> extends CompositionImpl<T> {

    @Inspect
    private final List<Composition<T>> elements;

    public All(Iterable<? extends Composition<T>> elements) {
        this.elements = ImmutableList.copyOf(elements);
    }

    public All(Stream<? extends Composition<T>> elements) {
        this.elements = elements.collect(Collectors.toImmutableList());
    }

    @Override
    public boolean isConstant() {
        return elements.stream().allMatch(Composition::isConstant);
    }

    @Override
    public Stream<T> dependencies() {
        return elements.stream().flatMap(Composition::dependencies);
    }

    @Override
    public Stream<T> elements(ITransientQuery query) {
        return elements.stream().flatMap(e -> e.elements(query));
    }
}
