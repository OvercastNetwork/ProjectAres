package tc.oc.commons.core.reflect;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ForwardingSet;
import com.google.common.reflect.TypeToken;

/**
 * A {@link Set} of {@link TypeToken}s with the property that no member is
 * assignable to any other member of the set.
 *
 * Exactly how this property is maintained depends on the {@link #prune(TypeToken)}
 * method, which subclasses must implement.
 */
public abstract class MinimalTypeSet<T extends TypeToken<?>, C extends Class<?>> extends ForwardingSet<T> {

    private final Set<T> types = new HashSet<>();

    @Override protected Set<T> delegate() { return types; }

    protected abstract boolean prune(T type);

    @Override
    public boolean add(T type) {
        if(types.contains(type)) return false;
        if(!prune(type)) return false;
        types.add(type);
        return true;
    }

    public boolean add(C type) {
        return add((T) TypeToken.of(type));
    }

    public boolean add(Type type) {
        return add((T) TypeToken.of(type));
    }

    @Override
    public boolean addAll(Collection<? extends T> types) {
        boolean changed = false;
        for(T t : types) {
            if(add(t)) changed = true;
        }
        return changed;
    }

    public Set<C> toClassSet() {
        return types.stream()
                    .map(t -> (C) t.getRawType())
                    .collect(Collectors.toSet());
    }
}
