package tc.oc.commons.core.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class SupersetView<V> extends AbstractSet<V> {

    private final Iterable<Set<? extends V>> subsets;

    public SupersetView(Iterable<Set<? extends V>> subsets) {
        this.subsets = subsets;
    }

    @Override
    public Iterator<V> iterator() {
        final Set<V> seen = new HashSet<>();
        return Iterators.concat(
            Iterators.transform(
                subsets.iterator(),
                s -> Iterators.filter(
                    s.iterator(),
                    seen::add
                )
            )
        );
    }

    @Override
    public int size() {
        return Iterators.size(iterator());
    }

    @Override
    public boolean isEmpty() {
        return Iterables.all(subsets, Collection::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return Iterables.any(subsets, s -> s.contains(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Set<?> s = new HashSet<>(c);
        for(Set<?> subset : subsets) {
            s.removeAll(subset);
            if(s.isEmpty()) return true;
        }
        return false;
    }

    @Override
    public boolean add(V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
