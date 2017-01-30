package tc.oc.commons.core.collection;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Iterators;

public class FlatCollection<T> extends AbstractCollection<T> {

    private final Iterable<? extends Collection<T>> collections;

    public FlatCollection(Iterable<? extends Collection<T>> collections) {
        this.collections = collections;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.concat(Iterators.transform(collections.iterator(), Iterable::iterator));
    }

    @Override
    public int size() {
        int size = 0;
        for(Collection<T> c : collections) {
            size += c.size();
        }
        return size;
    }
}
