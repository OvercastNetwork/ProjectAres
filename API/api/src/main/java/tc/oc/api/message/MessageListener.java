package tc.oc.api.message;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import tc.oc.api.config.ApiConstants;
import tc.oc.api.queue.Delivery;
import tc.oc.api.queue.Queue;

/**
 * A {@link Queue} consumer that can handle multiple message types.
 * Message handler methods are annotated with {@link MessageListener.HandleMessage},
 * and must take a {@link Message} subtype as their first parameter.
 * They can optionally take a {@link Delivery} as the second parameter.
 */
public interface MessageListener {

    default boolean listenWhileSuspended() {
        return false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface HandleMessage {
        /**
         * Ignore messages with a protocol_version header other than this.
         * A version of -1 will accept all messages.
         */
        int protocolVersion() default ApiConstants.PROTOCOL_VERSION;
    }
}
