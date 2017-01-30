package tc.oc.pgm.compose;

import java.util.stream.Stream;

import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.filters.query.ITransientQuery;

/**
 * A structure of operators that generate a flat sequence of {@link T}s from a {@link ITransientQuery}.
 *
 * Different queries will generate different sequences, within the rules of the operators.
 */
public interface Composition<T> extends Inspectable {

    boolean isConstant();

    Stream<T> dependencies();

    Stream<T> elements(ITransientQuery query);
}

abstract class CompositionImpl<T> extends Inspectable.Impl implements Composition<T> {}