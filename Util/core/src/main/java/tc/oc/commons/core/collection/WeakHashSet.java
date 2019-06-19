package tc.oc.commons.core.collection;

import java.util.Collection;
import java.util.WeakHashMap;

public class WeakHashSet<E> extends ForwardingSet<E> {

    public WeakHashSet() {
        super(new WeakHashMap<>());
    }

    public WeakHashSet(Collection<? extends E> initial) {
        this();
        this.addAll(initial);
    }

}
