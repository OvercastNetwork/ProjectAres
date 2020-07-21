package tc.oc.api.queue;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import tc.oc.api.connectable.Connectable;
import tc.oc.api.message.Message;
import tc.oc.api.message.MessageHandler;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageService;
import tc.oc.api.message.NoSuchMessageException;
import tc.oc.api.message.AbstractMessageService;
import tc.oc.api.serialization.Pretty;
import tc.oc.commons.core.util.Threadable;
import tc.oc.minecraft.suspend.Suspendable;

/**
 * An AMQP queue.
 *
 * Declaration and binding (note that Queue can be instantiated without an AMQP connection):
 *
 *     final Queue MY_QUEUE = new Queue("my_queue", true, false, false, null);
 *     MY_QUEUE.declare(Api.get().queueClient());
 *
 * Binding:
 *
 *     MY_QUEUE.bind(Exchange.DIRECT); // queue name is default routing key
 *     MY_QUEUE.bind(Exchange.FANOUT);
 *     MY_QUEUE.bind(Exchange.TOPIC, "fun_stuff");
 *
 * Subscription with {@link MessageHandler}:
 *
 *     MY_QUEUE.subscribe(ServerReconfigure.class, new MessageHandler<ServerReconfigure>() {
 *           @Override
 *           public void handleDelivery(ServerReconfigure message, Delivery delivery) {
 *               // ...
 *           }
 *     }, false, syncExecutor);
 *
 * Subscription with {@link MessageListener}:
 *
 *     class MyWorker implements MessageListener {
 *         @HandleMessage
 *         public void handleHealthReport(ServerHealthReport message, Delivery delivery) {
 *             // ...
 *         }
 *
 *         @HandleMessage
 *         public void handlePlayerReport(ServerPlayerReport message, Delivery delivery) {
 *             // ...
 *         }
 *     }
 *
 *     MY_QUEUE.subscribe(new MyWorker(), syncExecutor);
 */
public class Queue extends AbstractMessageService implements MessageService, Connectable, Suspendable {

    @Inject protected Gson gson;
    @Inject @Pretty protected Gson prettyGson;
    @Inject protected QueueClient client;
    @Inject protected Exchange.Topic topic;
    
    protected static Threadable<Metadata> METADATA = new Threadable<>();

    protected final Consume consume;

    @Nullable String consumerTag;
    private MultiDispatcher dispatcher;

    public Consume consume() { return consume; }
    public String name() { return consume.name(); }
    public QueueClient client() { return client; }

    public Queue(Consume consume) {
        this.consume = consume;
    }

    @Override
    public void connect() throws IOException {
        logger.fine("Declaring queue");
        client.getChannel().queueDeclare(consume.name(), consume.durable(), consume.exclusive(), consume.autoDelete(), consume.arguments());
        dispatcher = new MultiDispatcher();
        consumerTag = client.getChannel().basicConsume(consume.name(), false, "", false, true, Collections.<String, Object>emptyMap(), dispatcher);
    }

    @Override
    public void disconnect() throws IOException {
        if(dispatcher != null) {
            if(consumerTag != null) {
                client.getChannel().basicCancel(consumerTag);
                consumerTag = null;
            }
            try {
                dispatcher.awaitTermination(5L, TimeUnit.SECONDS);
            } catch(InterruptedException | ExecutionException | TimeoutException e) {
                throw new IOException("Failed to shutdown " + this, e);
            }
        }
    }

    @Override
    public void suspend() {
        suspended = true;
    }

    @Override
    public void resume() {
        suspended = false;
    }

    protected void bind(Exchange exchange, String routingKey) {
        try {
            logger.fine("Binding to exchange " + exchange.name() + " with routing key " + routingKey);

            client.getChannel().queueBind(name(), exchange.name(), routingKey, null);
        } catch(IOException e) {
            throw new IllegalStateException("Failed to bind to exchange " + exchange.name(), e);
        }
    }

    @Override
    public void bind(Class<? extends Message> type) {
        bind(topic, messageRegistry.typeName(type));
    }

    private class MultiDispatcher extends DefaultConsumer {

        private SettableFuture cancelled;

        MultiDispatcher() {
            super(client.getChannel());
        }

        void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if(cancelled != null) {
                cancelled.get(timeout, unit);
            }
        }

        @Override
        public void handleConsumeOk(String consumerTag) {
            cancelled = SettableFuture.create();
        }

        @Override
        public void handleCancelOk(String consumerTag) {
            if(cancelled != null) {
                cancelled.set(true);
            }
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, final AMQP.BasicProperties amqProperties, byte[] body) throws IOException {
            try {
                client.getChannel().basicAck(envelope.getDeliveryTag(), false);

                final TypeToken<? extends Message> type;
                try {
                    type = messageRegistry.resolve(amqProperties.getType(), Metadata.modelName(amqProperties));
                } catch(NoSuchMessageException e) {
                    // Probably a newer protocol
                    logger.warning("Skipping unknown message type: " + e.getMessage());
                    return;
                }

                final Collection<RegisteredHandler<?>> matchingHandlers;
                synchronized(handlers) {
                    matchingHandlers = handlersByType.allAssignableFrom(type);
                }
                if(matchingHandlers.isEmpty()) return;

                final String json = new String(body, Charsets.UTF_8);
                final Message message = gson.fromJson(json, type.getType());
                final Metadata properties = new Metadata(amqProperties);
                final Delivery delivery = new Delivery(client, consumerTag, envelope);

                if(logger.isLoggable(Level.FINE)) {
                    logger.fine("Received message " + properties.getType() + "\nMetadata: " + properties + "\n" + prettyGson.toJson(json));
                }
                
                METADATA.let(properties, () -> dispatchMessage(message, type));
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception dispatching AMQP message", t);
                // Don't let any exceptions through to the AMQP driver or it will close the channel
            }
        }
    }
}
