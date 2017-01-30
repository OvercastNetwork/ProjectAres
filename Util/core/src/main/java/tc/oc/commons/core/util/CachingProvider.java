package tc.oc.commons.core.util;

import javax.annotation.Nullable;
import javax.inject.Provider;

public class CachingProvider<T> implements Provider<T> {

    private final Provider<T> delegate;
    private volatile @Nullable T value;

    public CachingProvider(Provider<T> delegate) {
        this.delegate = delegate;
    }

    protected @Nullable T cachedValue() {
        return value;
    }

    synchronized protected T freshValue() {
        return value = delegate.get();
    }

    synchronized public void invalidate() {
        value = null;
    }

    @Override
    public T get() {
        T v = cachedValue();
        if(v != null) return v;
        synchronized(this) {
            v = cachedValue();
            return v != null ? v : freshValue();
        }
    }
}
