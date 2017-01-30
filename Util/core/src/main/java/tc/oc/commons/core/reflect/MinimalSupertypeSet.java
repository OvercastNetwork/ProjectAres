package tc.oc.commons.core.reflect;

import java.util.Iterator;

import com.google.common.reflect.TypeToken;

/**
 * A {@link MinimalTypeSet} that guarantees that, immediately after a type is added to the set,
 * there is at least one member of the set that is assignable to that type.
 */
public class MinimalSupertypeSet<T> extends MinimalTypeSet<TypeToken<? super T>, Class<? super T>> {
    @Override
    protected boolean prune(TypeToken<? super T> adding) {
        for(Iterator<TypeToken<? super T>> iterator = iterator(); iterator.hasNext(); ) {
            final TypeToken<? super T> existing = iterator.next();
            if(adding.isAssignableFrom(existing)) return false;
            if(existing.isAssignableFrom(adding)) iterator.remove();
        }
        return true;
    }
}
