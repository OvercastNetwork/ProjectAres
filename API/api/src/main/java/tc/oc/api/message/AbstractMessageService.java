package tc.oc.api.message;

import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.reflect.Methods;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.CachingTypeMap;

public abstract class AbstractMessageService implements MessageService {
    
    protected static class RegisteredHandler<T extends Message> {
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
    @Inject protected ExceptionHandler exceptionHandler;
    
    protected final Set<RegisteredHandler<?>> handlers = new HashSet<>();
    public final CachingTypeMap<Message, RegisteredHandler<?>> handlersByType = CachingTypeMap.create();
    protected volatile boolean suspended;
    
    @Inject void init(Loggers loggers) {
        logger = loggers.get(getClass());
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
        if(method.getParameterTypes().length != 1) {
            throw new IllegalStateException("Message handler method must take 1 parameter");
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
                    public void handleDelivery(Message message, TypeToken type) {
                        try {
                            method.invoke(listener, message);
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
    
    protected void dispatchMessage(Message message, TypeToken<? extends Message> type) {
        final Collection<RegisteredHandler<?>> matchingHandlers;
        synchronized (handlers) {
            matchingHandlers = handlersByType.allAssignableFrom(type);
        }
        if (matchingHandlers.isEmpty()) return;
        
        for (final RegisteredHandler handler : matchingHandlers) {
            if (suspended && handler.listener != null &&
                    !handler.listener.listenWhileSuspended()) continue;

            logger.fine("Dispatching " + type.getType() + " to " + handler.handler.getClass());
            if (handler.executor == null) {
                exceptionHandler.run(() -> handler.handler.handleDelivery(message, type));
            } else {
                handler.executor.execute(() -> {
                    synchronized (handlers) {
                        // Double check from the handler's executor that it is still registered.
                        // This makes it much less likely to dispatch a message to a handler
                        // after it unsubs. It should work perfectly if the handler unsubs on
                        // the same thread it handles messages on.
                        if (!handlers.contains(handler)) return;
                    }
                    exceptionHandler.run(() -> handler.handler.handleDelivery(message, type));
                });
            }
        }
    }
}
