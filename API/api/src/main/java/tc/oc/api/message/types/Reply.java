package tc.oc.api.message.types;

import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.message.Message;

/**
 * Generic reply with a success flag and optional error message.
 * This can used on its own, or subclassed to add more info.
 *
 * The API should always send back some subtype of this in response
 * to any message with the reply-to set in the AMQP metadata. Use a
 * {@link tc.oc.api.queue.Transaction} to send such a message and
 * listen for the reply.
 *
 * If the queue consumer does not explicitly reply, a generic success
 * reply will be generated. If the consumer raises an exception while
 * handling the message, a failed reply will be generated.
 *
 *
 */
@Serialize
public interface Reply extends Message {
    boolean success();
    @Nullable String error();

    Reply SUCCESS = new Reply() {
        @Override public boolean success() { return true; }
        @Override public @Nullable String error() { return null; }
    };

    Reply FAILURE = new Reply() {
        @Override public boolean success() { return false; }
        @Override public String error() { return "Failure!"; }
    };
}
