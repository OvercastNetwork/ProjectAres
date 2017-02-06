package tc.oc.api.connectable;

import java.io.IOException;

import com.google.inject.binder.ScopedBindingBuilder;

/**
 * Service that needs to be connected and disconnected along with the API
 *
 * Registration happens automatically the first time any {@link Connectable}
 * instance is provisioned through Guice. If this happens before or during
 * the connection process, the instance will be connected in the same order
 * that it was provisioned, with respect to other {@link Connectable}s.
 *
 * If a new {@link Connectable} instance is provisioned after the connection
 * phase is complete, an exception is thrown. To ensure that a {@link Connectable}
 * is provisioned in time to be connected, it is usually scoped with
 * {@link ScopedBindingBuilder#asEagerSingleton()}
 */
public interface Connectable {
    default void connect() throws IOException {};
    default void disconnect() throws IOException {};
}
