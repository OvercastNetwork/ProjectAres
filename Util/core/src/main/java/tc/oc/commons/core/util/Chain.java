package tc.oc.commons.core.util;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import tc.oc.commons.core.collection.IterableHelper;

/**
 * Append-only linked list
 */
public abstract class Chain<E> extends AbstractCollection<E> implements IterableHelper<E> {
    Chain() {}

    public Chain<E> push(E e) {
        return new Link<>(e, this);
    }

    public abstract @Nullable E peek();

    public abstract E head() throws NoSuchElementException;

    public abstract Chain<E> tail() throws NoSuchElementException;

    public E getFirst() throws NoSuchElementException {
        return head();
    }

    public Chain<E> removeFirst() throws NoSuchElementException {
        return tail();
    }

    public static <E> Chain<E> empty() {
        return Empty.INSTANCE;
    }
}

class Empty<E> extends Chain<E> {
    static final Empty INSTANCE = new Empty();

    private Empty() {}

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public @Nullable E peek() {
        return null;
    }

    @Override
    public E head() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    @Override
    public Chain<E> tail() throws NoSuchElementException {
        throw new NoSuchElementException();
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.emptyIterator();
    }
}

class Link<E> extends Chain<E> {
    final Chain<E> tail;
    final E value;
    final int size;

    Link(E value, Chain<E> tail) {
        this.tail = tail;
        this.value = value;
        this.size = 1 + tail.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Chain<E> tail() throws NoSuchElementException {
        return tail;
    }

    @Override
    public boolean contains(Object o) {
        return Objects.equals(value, o) || tail.contains(o);
    }

    @Override
    public E peek() {
        return value;
    }

    @Override
    public E head() throws NoSuchElementException {
        return value;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Chain<E> chain = Link.this;

            @Override
            public boolean hasNext() {
                return !chain.isEmpty();
            }

            @Override
            public E next() {
                final E e = chain.head();
                chain = chain.tail();
                return e;
            }
        };
    }
}


