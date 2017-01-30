package tc.oc.api.message.types;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

/**
 * Generic request used for testing. The {@link #reply_with} property
 * tells the API what to do in response:
 *
 *     success:     Do nothing, which should result in an automatic successful {@link Reply}
 *     failure:     Exclicitly send back a failed {@link Reply}
 *     exception:   Raise an exception, which should generate a failed {@link Reply}
 */
@Serialize
@MessageDefaults.ExpirationMillis(30000)
@MessageDefaults.Persistent(false)
public class Ping implements Message {
    public enum ReplyWith { success, failure, exception }

    public final ReplyWith reply_with;

    public Ping(ReplyWith reply_with) {
        this.reply_with = reply_with;
    }

    public Ping() {
        this(ReplyWith.success);
    }
}
