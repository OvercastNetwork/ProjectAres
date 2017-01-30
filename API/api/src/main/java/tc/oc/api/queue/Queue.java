package tc.oc.api.queue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import tc.oc.api.message.MessageQueue;
import tc.oc.api.message.MessageRegistry;
import tc.oc.api.message.NoSuchMessageException;
import tc.oc.api.serialization.Pretty;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.CachingTypeMap;
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
public class Queue implements MessageQueue, Connectable, Suspendable {

    private static class RegisteredHandler<T extends Message> {
        final @Nullable MessageListener listener;
        final MessageHandler<T> handler;
        final @Nullable Executor executor;

        private RegisteredHandler(@Nullable MessageListener listener, MessageHandler<T> handler, @Nullable Executor executor) {
            this.listener = listener;
            this.handler = handler;
            this.executor = executor;
        }
    }

    protected Logger logger;
    @Inject protected MessageRegistry messageRegistry;
    @Inject protected Gson gson;
    @Inject @Pretty protected Gson prettyGson;
    @Inject protected QueueClient client;
    @Inject protected Exchange.Topic topic;
    @Inject protected ExceptionHandler exceptionHandler;

    protected final Consume consume;

    @Nullable String consumerTag;
    private MultiDispatcher dispatcher;

    private final Set<RegisteredHandler<?>> handlers = new HashSet<>();
    private final CachingTypeMap<Message, RegisteredHandler<?>> handlersByType = CachingTypeMap.create();
    private volatile boolean suspended;

    public Consume consume() { return consume; }
    public String name() { return consume.name(); }
    public QueueClient client() { return client; }

    public Queue(Consume consume) {
        this.consume = consume;
    }

    @Inject void init(Loggers loggers) {
        logger = loggers.get(getClass());
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

    @Override
    public <T extends Message> void subscribe(TypeToken<T> messageType, MessageHandler<T> handler, @Nullable Executor executor) {
        subscribe(messageType, null, handler, executor);
    }

    private <T extends Message> void subscribe(TypeToken<T> messageType, @Nullable MessageListener listener, MessageHandler<T> handler, @Nullable Executor executor) {
        logger.fine("Subscribing handler " + handler);
        synchronized(handlers) {
            final RegisteredHandler<T> registered = new RegisteredHandler<>(listener, handler, executor);
            handlers.add(registered);
            handlersByType.put(messageType, registered);
            handlersByType.invalidate();
        }
    }

    private TypeToken<? extends Message> getMessageType(TypeToken decl, Method method) {
        if(method.getParameterTypes().length < 1 || method.getParameterTypes().length > 3) {
            throw new IllegalStateException("Message handler method must take 1 to 3 parameters");
        }

        final TypeToken<Message> base = new TypeToken<Message>(){};

        for(Type param : method.getGenericParameterTypes()) {
            final TypeToken paramToken = decl.resolveType(param);
            Types.assertFullySpecified(paramToken);
            if(base.isAssignableFrom(paramToken)) {
                messageRegistry.typeName(paramToken.getRawType()); // Verify message type is registered
                return paramToken;
            }
        }

        throw new IllegalStateException("Message handler has no message parameter");
    }

    @Override
    public void subscribe(final MessageListener listener, @Nullable Executor executor) {
        logger.fine("Subscribing listener " + listener);

        final TypeToken<? extends MessageListener> listenerType = TypeToken.of(listener.getClass());
        Methods.declaredMethodsInAncestors(listener.getClass()).forEach(method -> {
            final MessageListener.HandleMessage annot = method.getAnnotation(MessageListener.HandleMessage.class);
            if(annot != null) {
                method.setAccessible(true);
                final TypeToken<? extends Message> messageType = getMessageType(listenerType, method);

                logger.fine("  dispatching " + messageType.getRawType().getSimpleName() + " to method " + method.getName());

                MessageHandler handler = new MessageHandler() {
                    @Override
                    public void handleDelivery(Message message, TypeToken type, Metadata properties, Delivery delivery) {
                        try {
                            if(annot.protocolVersion() != -1 && annot.protocolVersion() != properties.protocolVersion()) {
                                return;
                            }

                            final Class<?>[] paramTypes = method.getParameterTypes();
                            Object[] params = new Object[paramTypes.length];
                            for(int i = 0; i < paramTypes.length; i++) {
                                if(paramTypes[i].isAssignableFrom(message.getClass())) {
                                    params[i] = message;
                                } else if(paramTypes[i].isAssignableFrom(Metadata.class)) {
                                    params[i] = properties;
                                } else if(paramTypes[i].isAssignableFrom(Delivery.class)) {
                                    params[i] = delivery;
                                }
                            }
                            method.invoke(listener, params);
                        } catch(IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        } catch(InvocationTargetException e) {
                            if(e.getCause() instanceof RuntimeException) {
                                throw (RuntimeException) e.getCause();
                            } else {
                                throw new IllegalStateException(e);
                            }
                        }
                    }

                    @Override
                    public String toString() {
                        return listener + "." + method.getName();
                    }
                };

                subscribe(messageType, listener, handler, executor);
            }
        });
    }

    @Override
    public void unsubscribe(MessageHandler<?> handler) {
        synchronized(handlers) {
            handlers.removeIf(registered -> registered.handler == handler);
            handlersByType.entries().removeIf(registered -> registered.getValue().handler == handler);
        }
    }

    @Override
    public void unsubscribe(MessageListener listener) {
        if(listener == null) return;
        synchronized(handlers) {
            handlers.removeIf(registered -> registered.listener == listener);
            handlersByType.entries().removeIf(registered -> registered.getValue().listener == listener);
        }
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

                for(final RegisteredHandler handler : matchingHandlers) {
                    if(suspended && handler.listener != null &&
                       !handler.listener.listenWhileSuspended()) continue;

                    logger.fine("Dispatching " + amqProperties.getType() + " to " + handler.handler.getClass());
                    if(handler.executor == null) {
                        exceptionHandler.run(() -> handler.handler.handleDelivery(message, type, properties, delivery));
                    } else {
                        handler.executor.execute(() -> {
                            synchronized(handlers) {
                                // Double check from the handler's executor that it is still registered.
                                // This makes it much less likely to dispatch a message to a handler
                                // after it unsubs. It should work perfectly if the handler unsubs on
                                // the same thread it handles messages on.
                                if(!handlers.contains(handler)) return;
                            }
                            exceptionHandler.run(() -> handler.handler.handleDelivery(message, type, properties, delivery));
                        });
                    }
                }
            } catch(Throwable t) {
                logger.log(Level.SEVERE, "Exception dispatching AMQP message", t);
                // Don't let any exceptions through to the AMQP driver or it will close the channel
            }
        }
    }
}
