package tc.oc.commons.core.util;

import com.google.common.collect.ForwardingObject;

public abstract class Forwarding<T> extends ForwardingObject {

    private final T delegate;

    protected Forwarding(T delegate) {
        this.delegate = delegate;
    }

    @Override
    protected T delegate() {
        return delegate;
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate().equals(obj);
    }
}
