package tc.oc.pgm.compose;

import java.util.stream.Stream;

import tc.oc.pgm.filters.query.ITransientQuery;

public class Unit<T> extends CompositionImpl<T> {

    @Inspect
    private final T element;

    public Unit(T element) {
        this.element = element;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public Stream<T> dependencies() {
        return Stream.of(element);
    }

    @Override
    public Stream<T> elements(ITransientQuery query) {
        return Stream.of(element);
    }
}
