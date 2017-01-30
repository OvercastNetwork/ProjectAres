package tc.oc.api.connectable;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.ExceptionUtils;

import static com.google.common.base.Preconditions.checkState;
import static tc.oc.commons.core.IterableUtils.reverseForEach;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

@Singleton
public class Connector implements PluginFacet {

    protected final Logger logger;
    private final ExceptionHandler exceptionHandler;
    private final Set<Connectable> services;
    private boolean connected;

    @Inject
    Connector(Loggers loggers, ExceptionHandler exceptionHandler, Set<Connectable> services) {
        this.exceptionHandler = exceptionHandler;
        this.services = services;
        this.logger = loggers.get(getClass());
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

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void enable() {
        checkState(!connected, "already connected");
        connected = true;
        logger.fine(() -> "Connecting all services");
        ExceptionUtils.propagate(() -> services.forEach(rethrowConsumer(this::connect)));
    }

    @Override
    public void disable() {
        checkState(connected, "not connected");
        connected = false;
        logger.fine(() -> "Disconnecting all services");
        reverseForEach(services, service -> exceptionHandler.run(() -> disconnect(service)));
    }
}
