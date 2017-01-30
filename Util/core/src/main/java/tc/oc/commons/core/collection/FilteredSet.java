package tc.oc.commons.core.collection;

import java.util.Collection;

import com.google.common.collect.ForwardingSet;

/**
 * A {@link ForwardingSet} that delegates all insertion operations to {@link #addInternal}
 */
public abstract class FilteredSet<E> extends ForwardingSet<E> {

    protected abstract boolean addInternal(E element);

    @Override
    public boolean add(E element) {
        return addInternal(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return standardAddAll(collection);
    }
}
