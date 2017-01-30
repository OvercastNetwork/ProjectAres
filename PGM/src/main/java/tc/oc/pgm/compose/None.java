package tc.oc.pgm.compose;

import java.util.stream.Stream;

import tc.oc.pgm.filters.query.ITransientQuery;

public class None<T> extends CompositionImpl<T> {

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Stream<T> dependencies() {
        return Stream.empty();
    }

    @Override
    public Stream<T> elements(ITransientQuery query) {
        return Stream.empty();
    }
}
