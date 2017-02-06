package tc.oc.api.connectable;

import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.ExceptionUtils;
import tc.oc.minecraft.api.event.Enableable;

import static com.google.common.base.Preconditions.checkState;

@Singleton
class Connector implements Enableable {

    protected final Logger logger;
    private final ExceptionHandler exceptionHandler;
    private final Set<Connectable> registered = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Deque<Connectable> pending = new LinkedList<>();
    private final Deque<Connectable> connected = new LinkedList<>();
    private boolean finishedConnecting;

    @Inject Connector(Loggers loggers, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.logger = loggers.get(getClass());
    }

    void register(Connectable connectable) {
        if(registered.add(connectable)) {
            if(finishedConnecting) {
                throw new IllegalStateException("Tried to provision a " + Connectable.class.getSimpleName() +
                                                " when already connected");
            }
            pending.add(connectable);
        }
    }

    private void connect(Connectable service) throws IOException {
        if(service.isActive()) {
            logger.fine(() -> "Connecting " + service.getClass().getName());
            service.connect();
        }
    }

    private void disconnect(Connectable service) throws IOException {
        if(service.isActive()) {
            logger.fine(() -> "Disconnecting " + service.getClass().getName());
            service.disconnect();
        }
    }

    @Override
    public void enable() {
        checkState(!finishedConnecting, "already connected");
        logger.fine(() -> "Connecting all services");
        for(;;) {
            final Connectable connectable = pending.poll();
            if(connectable == null) break;
            ExceptionUtils.propagate(() -> connect(connectable));
            connected.push(connectable);
        }
        finishedConnecting = true;
    }

    @Override
    public void disable() {
        checkState(finishedConnecting, "not connected");
        logger.fine(() -> "Disconnecting all services");
        while(!connected.isEmpty()) {
            exceptionHandler.run(() -> disconnect(connected.pop()));
        }
    }
}
