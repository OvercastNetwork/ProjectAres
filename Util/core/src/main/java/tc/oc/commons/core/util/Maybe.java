package tc.oc.commons.core.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Mutable equivalent of {@link Optional} that implements {@link Set}.
 *
 * The interface provides default implementations of all methods except
 * {@link #orElse(Object)}, {@link #set(Object)}, and {@link #clear()}.
 *
 * You should also implement {@link #equals(Object)} and {@link #hashCode()}
 * to match the contract specified in {@link Set} (which is not allowed
 * in an interface, unfortunately).
 */
public interface Maybe<E> extends Set<E> {

    E orElse(E that);

    void set(E value);

    default E get() {
        final E e = orElse(null);
        if(e == null) {
            throw new NoSuchElementException("No value present");
        }
        return e;
    }

    default boolean isPresent() {
        return orElse(null) != null;
    }

    default Optional<E> toOptional() {
        return Optional.ofNullable(orElse(null));
    }

    @Override
    default int size() {
        return isPresent() ? 1 : 0;
    }

    @Override
    default boolean isEmpty() {
        return !isPresent();
    }

    @Override
    default boolean contains(Object e) {
        final E value = orElse(null);
        return value != null && value.equals(e);
    }

    @Override
    default Iterator<E> iterator() {
        return new Iterator<E>() {
            boolean done;

            @Override
            public boolean hasNext() {
                return !done && isPresent();
            }

            @Override
            public E next() {
                final E value = orElse(null);
                if(value == null) {
                    throw new NoSuchElementException();
                }
                done = true;
                return value;
            }
        };
    }

    @Override
    default Object[] toArray() {
        final E value = orElse(null);
        return value == null ? ArrayUtils.zeroObjects()
                             : new Object[] {value};
    }

    @Override
    default <T> T[] toArray(T[] a) {
        final E value = orElse(null);
        if(value != null) {
            if(a.length < 1) {
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), 1);
            }
            a[0] = (T) value;
        }
        return a;
    }

    @Override
    default boolean add(E e) {
        if(e == null) throw new NullPointerException();

        final E value = orElse(null);

        if(value == null) {
            set(e);
            return true;
        } else if(value.equals(e)) {
            return false;
        } else {
            throw new IllegalStateException("A different value is already present");
        }
    }

    @Override
    default boolean remove(Object e) {
        if(contains(e)) {
            clear();
            return true;
        }
        return false;
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        final E value = orElse(null);
        if(value == null) {
            return c.isEmpty();
        } else {
            for(Object e : c) {
                if(!value.equals(e)) return false;
            }
            return true;
        }
    }

    @Override
    default boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for(Object e : c) {
            if(add((E) e)) changed = true;
        }
        return changed;
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        final E value = orElse(null);
        if(value == null || c.contains(value)) return false;
        clear();
        return true;
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        final E value = orElse(null);
        if(value == null || !c.contains(value)) return false;
        clear();
        return true;
    }

    class Impl<E> implements Maybe<E> {
        private E value;

        private Impl() {}

        private Impl(E value) {
            set(value);
        }

        @Override
        public boolean equals(Object that) {
            if(this == that) return true;
            if(!(that instanceof Set)) return false;

            final E value = orElse(null);
            final Iterator it = ((Set) that).iterator();
            if(value == null) {
                return !it.hasNext();
            } else
                return it.hasNext() && value.equals(it.next());
        }

        @Override
        public int hashCode() {
            final E value = orElse(null);
            return value == null ? 0 : value.hashCode();
        }

        @Override
        public E orElse(E that) {
            final E value = this.value;
            return value != null ? value : that;
        }

        @Override
        public void set(E value) {
            if(value == null) throw new NullPointerException();
            this.value = value;
        }

        @Override
        public void clear() {
            value = null;
        }
    }

    static <E> Maybe<E> empty() {
        return new Impl<>();
    }

    static <E> Maybe<E> of(E value) {
        return new Impl<>(value);
    }

    static <E> Maybe<E> ofNullable(@Nullable E value) {
        return value != null ? of(value) : empty();
    }

    static <E> Maybe<E> ofOptional(Optional<E> value) {
        return value.isPresent() ? of(value.get()) : empty();
    }
}
