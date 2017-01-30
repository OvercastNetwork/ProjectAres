package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Provider;

import com.google.inject.Key;
import com.google.inject.Scopes;

public class InjectionStore<A extends Annotation> {

    private final Map<Key<?>, Object> map;

    public InjectionStore() {
        this(new HashMap<>());
    }

    public InjectionStore(Map<Key<?>, Object> map) {
        this.map = map;
    }

    public <T> void store(Key<T> key, T value) {
        map.put(key, value);
    }

    public @Nullable <T> T provide(Key<T> key) {
        return (T) map.get(key);
    }

    public <T> T provide(Key<T> key, Provider<T> provider) {
        T t = (T) map.get(key);
        if(t != null) return t;

        t = provider.get();
        if(!Scopes.isCircularProxy(t)) {
            store(key, t);
        }

        return t;
    }
}
