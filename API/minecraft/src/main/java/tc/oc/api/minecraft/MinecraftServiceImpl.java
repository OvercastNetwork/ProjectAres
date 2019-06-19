package tc.oc.api.minecraft;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.connectable.Connectable;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.exceptions.ApiNotConnected;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.api.minecraft.config.MinecraftApiConfiguration;
import tc.oc.api.minecraft.servers.LocalServerDocument;
import tc.oc.api.minecraft.servers.LocalServerReconfigureEvent;
import tc.oc.api.minecraft.servers.StartupServerDocument;
import tc.oc.api.servers.ServerService;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.util.MethodHandleInvoker;
import tc.oc.commons.core.util.ProxyUtils;
import tc.oc.minecraft.scheduler.SyncExecutor;

@Singleton
public class MinecraftServiceImpl implements MinecraftService, MessageListener, Connectable {

    private final Logger logger;
    private final EventBus eventBus;
    private final SyncExecutor syncExecutor;
    private final ServerService serverService;
    private final MinecraftApiConfiguration apiConfiguration;
    private final StartupServerDocument startupDocument;
    private final MessageQueue serverQueue;
    private final Server everfreshLocalServer;

    private @Nullable Server server;

    @Inject MinecraftServiceImpl(Loggers loggers,
                                 EventBus eventBus,
                                 SyncExecutor syncExecutor,
                                 ServerService serverService,
                                 MinecraftApiConfiguration apiConfiguration,
                                 MessageQueue serverQueue,
                                 LocalServerDocument localServerDocument,
                                 StartupServerDocument startupDocument) {

        this.logger = loggers.get(getClass());
        this.eventBus = eventBus;
        this.syncExecutor = syncExecutor;
        this.serverService = serverService;
        this.apiConfiguration = apiConfiguration;
        this.serverQueue = serverQueue;

        this.everfreshLocalServer = ProxyUtils.newProxy(Server.class, new MethodHandleInvoker() {
            @Override
            protected Object targetFor(Method method) {
                if(server != null) return server;
                if(Methods.respondsTo(localServerDocument, method)) return localServerDocument;
                throw new ApiNotConnected();
            }
        });
        this.startupDocument = startupDocument;
    }

    @Override
    public boolean listenWhileSuspended() {
        return true;
    }

    private void assertConnected() throws ApiNotConnected {
        if(server == null) {
            throw new ApiNotConnected();
        }
    }

    @Override
    public Server getLocalServer() {
        assertConnected();
        return server;
    }

    @Override
    public boolean isLocalServer(ServerDoc.Identity server) {
        return getLocalServer()._id().equals(server._id());
    }

    /**
     * Return a magic {@link Server} document for the local server that
     * always has the most recent data. If the API is not connected,
     * the fields provided by {@link LocalServerDocument} will work,
     * but trying to read any other field will throw {@link ApiNotConnected}.
     */
    @Override
    public Server everfreshLocalServer() {
        return everfreshLocalServer;
    }

    /**
     * Send a server configuration change to the remote API. The API will respond with
     * the latest version of the server document, and only at that point will the local
     * document be modified. This is done by calling {@link #handleReconfigure},
     * and subclasses can override that method if they want to fire a reconfigure event.
     *
     * This method returns a future that completes after the API responds to the update
     * AND the local server document has been replaced with the result.
     */
    @Override
    public ListenableFuture<Server> updateLocalServer(ServerDoc.Partial update) {
        return Futures.transform(
            serverService.update(apiConfiguration.serverId(), update),
            (Function<? super Server, ? extends Server>) result -> {
                handleLocalReconfigure(result);
                return result;
            },
            syncExecutor
        );
    }

    @HandleMessage
    public void handleReconfigure(ModelUpdate<Server> message) {
        if(server != null && server._id().equals(message.document()._id())) {
            handleLocalReconfigure(message.document());
        }
    }

    protected void handleLocalReconfigure(Server newConfig) {
        Server oldConfig = this.server;
        this.server = newConfig;
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Local server reconfigured: " + newConfig);
        }
        eventBus.post(new LocalServerReconfigureEvent(oldConfig, newConfig));
    }

    @Override
    public void connect() throws IOException {
        try {
            serverQueue.subscribe(this, syncExecutor);

            handleLocalReconfigure(serverService.update(apiConfiguration.serverId(), startupDocument).get());

            logger.info("Connected to API as server." + getLocalServer()._id());

            if(apiConfiguration.publishIp()) {
                String oldIp = server.ip(), newIp = startupDocument.ip();
                if(!Objects.equals(oldIp, newIp)) {
                    updateLocalServer((ServerDoc.Ip) () -> newIp).get();
                    logger.info("Changed ip from " + oldIp + " to " + newIp);
                }
            }
        } catch (Exception e) {
            this.processIntoIOException(e);
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            serverService.update(
                apiConfiguration.serverId(),
                (ServerDoc.Online) () -> false
            ).get();

            serverQueue.unsubscribe(this);
        } catch (Exception e) {
            processIntoIOException(e);
        }
        this.server = null;
    }

    private void processIntoIOException(Throwable t) throws IOException {
        if (t instanceof IOException) throw (IOException)t;
        if (t instanceof ExecutionException) this.processIntoIOException(t.getCause());
        throw new IOException(t);
    }
}
