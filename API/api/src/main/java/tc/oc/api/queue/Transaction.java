package tc.oc.api.queue;

import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.Duration;
import tc.oc.api.exceptions.ApiException;
import tc.oc.api.message.Message;
import tc.oc.api.message.MessageHandler;
import tc.oc.api.message.types.Reply;
import tc.oc.commons.core.concurrent.TimeoutFuture;
import tc.oc.commons.core.util.ExceptionUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a {@link ListenableFuture} that sends a {@link Message} to an {@link Exchange}
 * and then waits to receive a reply on a given {@link Queue}. The reply-to header on the
 * request message is set to the name of the reply queue, and the eventual reply message
 * is identified by its correlation-id matching the message-id of the request. There is
 * also a timeout, which defaults to 30 seconds, that will cause the future to fail with
 * a {@link TimeoutException}.
 */
public class Transaction<T extends Message> extends TimeoutFuture<T> {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final String requestId;
    private final Class<? extends Message> requestType;
    private final Queue replyQueue;
    private final MessageHandler<Message> messageHandler;
    private final StackTraceElement[] callSite;

    public static class Factory {
        private final Exchange.Direct exchange;
        private final PrimaryQueue primaryQueue;

        @Inject Factory(Exchange.Direct exchange, PrimaryQueue primaryQueue) {
            this.exchange = exchange;
            this.primaryQueue = primaryQueue;
        }

        /**
         * @param request       Outgoing request message
         * @param properties    Metadata for the request (null for default properties)
         * @param publish       Publishing options for the request (null for default options)
         * @param replyType     Type of the reply message (if the reply isn't assignable to this, it will be missed)
         * @param timeout       Time to wait for the reply before failing (null for DEFAULT_TIMEOUT)
         */
        public <T extends Message> Transaction<T> request(Message request,
                                                          @Nullable Metadata properties,
                                                          @Nullable Publish publish,
                                                          TypeToken<T> replyType,
                                                          @Nullable Duration timeout) {

            return new Transaction<>(exchange, primaryQueue, request, properties, publish, replyType, timeout);
        }

        public <T extends Message> Transaction<T> request(Message request, @Nullable Metadata properties, @Nullable Publish publish, Class<T> replyType, @Nullable Duration timeout) {
            return request(request, properties, publish, TypeToken.of(replyType), timeout);
        }

        public <T extends Message> Transaction<T> request(Message request, TypeToken<T> replyType, @Nullable Publish publish) {
            return request(request, null, publish, replyType, null);
        }

        public <T extends Message> Transaction<T> request(Message request, Class<T> replyType, @Nullable Publish publish) {
            return request(request, null, publish, replyType, null);
        }

        public <T extends Message> Transaction<T> request(Message request, TypeToken<T> replyType, String routingKey) {
            return request(request, replyType, new Publish(routingKey));
        }

        public <T extends Message> Transaction<T> request(Message request, Class<T> replyType, String routingKey) {
            return request(request, replyType, new Publish(routingKey));
        }

        public <T extends Message> Transaction<T> request(Message request, TypeToken<T> replyType) {
            return request(request, replyType, (Publish) null);
        }

        public <T extends Message> Transaction<T> request(Message request, Class<T> replyType) {
            return request(request, replyType, (Publish) null);
        }

        public Transaction<Reply> request(Message request, @Nullable Publish publish) {
            return request(request, Reply.class, publish);
        }

        public Transaction<Reply> request(Message request, String routingKey) {
            return request(request, new Publish(routingKey));
        }

        public Transaction<Reply> request(Message request) {
            return request(request, (Publish) null);
        }
    }

    private Transaction(Exchange.Direct exchange,
                        final Queue replyQueue,
                        Message request,
                        @Nullable Metadata requestProps,
                        @Nullable Publish publish,
                        TypeToken<T> replyType,
                        @Nullable Duration timeout) {

        super(timeout != null ? timeout : DEFAULT_TIMEOUT);

        checkNotNull(request, "request");
        checkNotNull(replyType, "replyType");

        this.callSite = new Exception().getStackTrace();
        this.replyQueue = replyQueue;

        final Metadata finalRequestProps = requestProps = replyQueue.client().getProperties(request, requestProps);
        finalRequestProps.setReplyTo(this.replyQueue.name());

        this.requestId = checkNotNull(finalRequestProps.getMessageId());
        this.requestType = request.getClass();

        this.messageHandler = new MessageHandler<Message>() {
            @Override public void handleDelivery(Message message, TypeToken<? extends Message> type, Metadata replyProps, Delivery delivery) {
                if(requestId.equals(replyProps.getCorrelationId())) {
                    Transaction.this.replyQueue.unsubscribe(messageHandler);

                    if(replyProps.protocolVersion() != finalRequestProps.protocolVersion()) {
                        setException(new ApiException("Received a protocol " + replyProps.protocolVersion() +
                                                      " reply to a protocol " + finalRequestProps.protocolVersion() + " request",
                                                      callSite));
                    } else {
                        final Reply reply = message instanceof Reply ? (Reply) message : null;
                        if(reply != null && !reply.success()) {
                            setException(new ApiException(reply.error() != null ? reply.error()
                                                                                : "Generic failure reply to request " + requestId,
                                                          callSite));
                        } else if(!replyType.isAssignableFrom(type)) {
                            setException(new ApiException("Wrong reply type to request " + requestId +
                                                          ", expected a " + replyType +
                                                          ", received a " + type,
                                                          callSite));
                        } else {
                            set((T) message);
                        }
                    }
                }
            }
        };

        this.replyQueue.subscribe(Message.class, messageHandler, null);

        exchange.publishAsync(request, finalRequestProps, publish);
    }

    @Override
    protected void interruptTask() {
        replyQueue.unsubscribe(messageHandler);
    }

    @Override
    protected String makeTimeoutMessage() {
        return "Timed out waiting for reply to request " + requestId + " (" + requestType +
               ") created at " + ExceptionUtils.formatStackTrace(callSite);
    }
}
