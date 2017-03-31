package tc.oc.commons.core.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ForwardingSet<E> implements Set<E> {

    protected final Map<E, ?> backend;

    public ForwardingSet(Map<E, ?> backend) {
        this.backend = backend;
    }

    @Override
    public int size() {
        return backend.size();
    }

    @Override
    public boolean isEmpty() {
        return backend.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return backend.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return backend.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return backend.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return backend.keySet().toArray(a);
    }

    @Override
    public boolean add(E e) {
        if(backend.containsKey(e)) {
            return false;
        } else {
            backend.put(e, null);
            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        if(!backend.containsKey(o)) {
            return false;
        } else {
            backend.remove(o);
            return true;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return c.stream().anyMatch(this::add);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return c.stream().filter(i -> !contains(i)).anyMatch(this::remove);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().anyMatch(this::remove);
    }

    @Override
    public void clear() {
        backend.clear();
    }

}
