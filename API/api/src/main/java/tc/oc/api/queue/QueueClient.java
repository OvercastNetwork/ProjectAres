package tc.oc.api.queue;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;
import tc.oc.api.connectable.Connectable;
import tc.oc.api.config.ApiConstants;
import tc.oc.api.message.Message;
import tc.oc.api.message.MessageRegistry;
import tc.oc.api.message.types.ModelMessage;
import tc.oc.api.model.IdFactory;
import tc.oc.api.model.ModelRegistry;
import tc.oc.commons.core.concurrent.ExecutorUtils;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.Joiners;
import tc.oc.commons.core.util.MapUtils;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class QueueClient implements Connectable {

    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

    protected static final Metadata DEFAULT_PROPERTIES = new Metadata.Builder()
        .appId("ocn")
        .contentType("application/json")
        .contentEncoding("utf-8")
        .protocolVersion(ApiConstants.PROTOCOL_VERSION)
        .build();

    protected final Logger logger;

    private final QueueClientConfiguration config;
    private final @Nullable ThreadFactory threadFactory;
    private final Gson gson;
    private final MessageRegistry messageRegistry;
    private final ModelRegistry modelRegistry;
    private final IdFactory idFactory;

    private Connection connection;
    private Channel channel;
    private final ListeningExecutorService executorService;

    @Inject QueueClient(Loggers loggers, QueueClientConfiguration config, Gson gson, MessageRegistry messageRegistry, ModelRegistry modelRegistry, IdFactory idFactory) {
        this.modelRegistry = modelRegistry;
        this.logger = loggers.get(getClass());
        this.idFactory = idFactory;
        this.config = checkNotNull(config, "config");
        this.gson = checkNotNull(gson, "GSON");
        this.messageRegistry = checkNotNull(messageRegistry, "message registry");
        this.threadFactory = new ThreadFactoryBuilder().setNameFormat("API AMQP Executor").build();

        if (config.getThreads() > 0) {
            this.executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(config.getThreads()));
        } else {
            this.executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(threadFactory));
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Channel getChannel() {
        if(channel == null) {
            throw new IllegalStateException("QueueClient is not connected");
        }
        return channel;
    }

    private static Metadata cloneProperties(Metadata properties) {
        try {
            return (Metadata) properties.clone();
        } catch(CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void mergeProperties(Metadata to, BasicProperties from) {
        if(from.getMessageId() != null) to.setMessageId(from.getMessageId());
        if(from.getDeliveryMode() != null) to.setDeliveryMode(from.getDeliveryMode());
        if(from.getExpiration() != null) to.setExpiration(from.getExpiration());
        if(from.getCorrelationId() != null) to.setCorrelationId(from.getCorrelationId());
        if(from.getReplyTo() != null) to.setReplyTo(from.getReplyTo());

        final Map<String, Object> headers = from.getHeaders();
        if(headers != null && !headers.isEmpty()) {
            to.setHeaders(MapUtils.merge(to.getHeaders(), headers));
        }
    }

    public Metadata getProperties(Message message, @Nullable BasicProperties properties) {
        Metadata amqp = cloneProperties(DEFAULT_PROPERTIES);

        amqp.setMessageId(idFactory.newId());
        amqp.setTimestamp(new Date());

        amqp.setType(messageRegistry.typeName(message.getClass()));
        if(message instanceof ModelMessage) {
            amqp.setHeaders(MapUtils.merge(amqp.getHeaders(),
                                           Metadata.MODEL_NAME,
                                           modelRegistry.meta(((ModelMessage) message).model()).name()));
        }

        MessageDefaults.ExpirationMillis expiration = Types.inheritableAnnotation(message.getClass(), MessageDefaults.ExpirationMillis.class);
        if(expiration != null) {
            amqp.setExpiration(String.valueOf(expiration.value()));
        }

        MessageDefaults.Persistent persistent = Types.inheritableAnnotation(message.getClass(), MessageDefaults.Persistent.class);
        if(persistent != null) {
            amqp.setDeliveryMode(persistent.value() ? 2 : 1);
        }

        if(properties != null) mergeProperties(amqp, properties);

        return amqp;
    }

    public Publish getPublish(Message message, @Nullable Publish publish) {
        if(publish == null) {
            publish = Publish.DEFAULT;
        }

        if("".equals(publish.routingKey())) {
            MessageDefaults.RoutingKey routingKey = Types.inheritableAnnotation(message.getClass(), MessageDefaults.RoutingKey.class);
            if(routingKey != null) {
                publish = new Publish(routingKey.value(), publish.mandatory(), publish.immediate());
            }
        }

        return publish;
    }

    private void publish(Exchange exchange, String payload, AMQP.BasicProperties properties, @Nullable Publish publish) {
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("Publishing to exchange " + exchange.name() +
                        " with routing key " + publish.routingKey() +
                        " and properties " + properties +
                        ":\n" + payload);
        }

        try {
            getChannel().basicPublish(exchange.name(),
                                      publish.routingKey(),
                                      publish.mandatory(),
                                      publish.immediate(),
                                      properties,
                                      payload.getBytes(Charsets.UTF_8));
        } catch(IOException e) {
            logger.log(Level.SEVERE,
                       "Failed to publish message of type " + properties.getType() +
                       " to exchange '" + exchange + "' with routing key '" + publish.routingKey() + "'",
                       e);
        }
    }

    public void publishSync(Exchange exchange, Message message, @Nullable BasicProperties properties, @Nullable Publish publish) {
        publish(exchange, gson.toJson(message), getProperties(message, properties), Publish.forMessage(message, publish));
    }

    public ListenableFuture<?> publishAsync(final Exchange exchange, final Message message, final @Nullable BasicProperties properties, final @Nullable Publish publish) {
        // NOTE: Serialization must happen synchronously, because getter methods may not be thread-safe
        final String payload = gson.toJson(message);
        final AMQP.BasicProperties finalProperties = getProperties(message, properties);
        final Publish finalPublish = Publish.forMessage(message, publish);

        if(this.executorService == null) throw new IllegalStateException("Not connected");
        return this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    publish(exchange, payload, finalProperties, finalPublish);
                } catch(Throwable e) {
                    logger.log(Level.SEVERE, "Unhandled exception publishing message type " + finalProperties.getType(), e);
                }
            }
        });
    }

    private ConnectionFactory createConnectionFactory() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(this.config.getUsername());
        factory.setPassword(this.config.getPassword());
        factory.setVirtualHost(this.config.getVirtualHost());

        factory.setAutomaticRecoveryEnabled(true);
        factory.setConnectionTimeout(this.config.getConnectionTimeout());
        factory.setNetworkRecoveryInterval(this.config.getNetworkRecoveryInterval());

        if (this.threadFactory != null) {
            factory.setThreadFactory(this.threadFactory);
        }

        return factory;
    }

    @Override
    public void connect() throws IOException {
        if(config.getAddresses().isEmpty()) {
            logger.warning("Skipping AMQP connection because no addresses are configured");
        } else {
            logger.info("Connecting to AMQP API at " + Joiners.onCommaSpace.join(config.getAddresses()));
            this.connection = this.createConnectionFactory().newConnection(this.config.getAddresses().toArray(new Address[0]));
            this.channel = this.connection.createChannel();
        }
    }

    @Override
    public void disconnect() throws IOException {
        ExecutorUtils.shutdownImpatiently(executorService, logger, SHUTDOWN_TIMEOUT);

        if(channel != null) {
            channel.close();
            connection.close();
        }
    }
}
