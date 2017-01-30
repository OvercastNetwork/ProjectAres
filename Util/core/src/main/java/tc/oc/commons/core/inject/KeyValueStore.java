package tc.oc.commons.core.inject;

import java.util.Map;
import javax.annotation.Nullable;

import com.google.inject.Key;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A map of Key<T> -> T
 *
 * Has various Guice related uses
 */
public interface KeyValueStore {

    int size();

    @Nullable <T> T get(Key<T> key);

    @Nullable <T> T put(Key<T> key, T value);

    @Nullable <T> T remove(Key<T> key);

    default boolean containsKey(Key<?> key) {
        return get(key) != null;
    }

    default boolean isEmpty() {
        return size() != 0;
    }

    class Impl implements KeyValueStore {
        private final Map<Key<?>, Object> map;

        public Impl(Map<Key<?>, Object> map) {
            checkArgument(map.isEmpty());
            this.map = map;
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean containsKey(Key<?> key) {
            return map.containsKey(key);
        }

        @Override
        public <T> T get(Key<T> key) {
            return (T) map.get(key);
        }

        @Override
        public <T> T put(Key<T> key, T value) {
            return (T) map.put(key, value);
        }

        @Override
        public <T> T remove(Key<T> key) {
            return (T) map.remove(key);
        }
    }
}
