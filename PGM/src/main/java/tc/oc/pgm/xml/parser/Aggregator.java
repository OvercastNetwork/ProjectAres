package tc.oc.pgm.xml.parser;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.IterableUtils;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.AmbiguousElementException;
import tc.oc.commons.core.util.DuplicateElementException;
import tc.oc.commons.core.util.Optionals;

/**
 * Combines a sequence of {@link I} instances into a single instance of {@link O},
 * which may be the same as {@link I}, or some container of {@link I}.
 *
 * Exceptions are thrown if the inner sequence does not fit the structure of the outer type:
 *
 *      {@link NoSuchElementException}      If the sequence is empty and the aggregate requires at least one element
 *      {@link AmbiguousElementException}   If the sequence has multiple elements and the aggregate requires no more than one element
 *      {@link DuplicateElementException}   If the sequence has duplicate elements and the aggregate requires unique elements
 */
public abstract class Aggregator<O, I> {

    private final TypeToken<O> outerType;
    private final TypeToken<I> innerType;

    protected Aggregator(TypeToken<I> innerType, TypeToken<O> outerType) {
        this.outerType = outerType;
        this.innerType = innerType;
    }

    public TypeToken<O> outerTypeToken() {
        return outerType;
    }

    public TypeToken<I> innerTypeToken() {
        return innerType;
    }

    public TypeLiteral<O> outerTypeLiteral() {
        return Types.toLiteral(outerType);
    }

    public TypeLiteral<I> innerTypeLiteral() {
        return Types.toLiteral(innerType);
    }

    protected abstract O aggregateElements(List<I> elements);

    public O aggregateElements(Stream<I> elements) throws NoSuchElementException,
                                                          AmbiguousElementException,
                                                          DuplicateElementException {

        return aggregateElements(elements.collect(Collectors.toList()));
    }

    public static boolean isWrapped(TypeToken<?> type) {
        return Types.isAssignable(Optional.class, type) ||
               Types.isAssignable(Iterable.class, type);
    }

    public static <T> Aggregator<T, ?> forType(TypeToken<T> type) {
        if(Types.isAssignable(Optional.class, type)) {
            return new OptionalAggregator(Optionals.elementType((TypeToken) type));
        }

        if(Types.isAssignable(Set.class, type)) {
            return new SetAggregator(IterableUtils.elementType((TypeToken) type));
        }

        if(Types.isAssignable(Iterable.class, type)) {
            return new ListAggregator(IterableUtils.elementType((TypeToken) type));
        }

        return new RequiredAggregator<>(type);
    }
}

class RequiredAggregator<T> extends Aggregator<T, T> {
    protected RequiredAggregator(TypeToken<T> innerType) {
        super(innerType, innerType);
    }

    @Override
    public T aggregateElements(List<T> elements) {
        switch(elements.size()) {
            case 0: throw new NoSuchElementException();
            case 1: return elements.get(0);
            default: throw new AmbiguousElementException();
        }
    }
}

class OptionalAggregator<T> extends Aggregator<Optional<T>, T> {
    protected OptionalAggregator(TypeToken<T> innerType) {
        super(innerType, new TypeToken<Optional<T>>(){}.where(new TypeParameter<T>(){}, innerType));
    }

    @Override
    public Optional<T> aggregateElements(List<T> elements) {
        switch(elements.size()) {
            case 0: return Optional.empty();
            case 1: return Optional.of(elements.get(0));
            default: throw new AmbiguousElementException("Multiple values provided");
        }
    }
}

class ListAggregator<T> extends Aggregator<List<T>, T> {
    protected ListAggregator(TypeToken<T> innerType) {
        super(innerType, new TypeToken<List<T>>(){}.where(new TypeParameter<T>(){}, innerType));
    }

    @Override
    public List<T> aggregateElements(List<T> elements) {
        return elements;
    }
}

class SetAggregator<T> extends Aggregator<Set<T>, T> {
    protected SetAggregator(TypeToken<T> innerType) {
        super(innerType, new TypeToken<Set<T>>(){}.where(new TypeParameter<T>(){}, innerType));
    }

    @Override
    public Set<T> aggregateElements(List<T> elements) {
        final Set<T> set = ImmutableSet.copyOf(elements);
        if(set.size() != elements.size()) {
            throw new DuplicateElementException();
        }
        return set;
    }
}