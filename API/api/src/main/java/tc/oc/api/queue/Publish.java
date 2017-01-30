package tc.oc.api.queue;

import javax.annotation.Nullable;

import tc.oc.api.message.Message;
import tc.oc.commons.core.reflect.Types;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Options for publishing a {@link Message} through a {@link Queue}
 */
public class Publish {
    public static final Publish DEFAULT = new Publish();

    private final String routingKey;
    private final boolean mandatory;
    private final boolean immediate;

    public Publish(String routingKey, boolean mandatory, boolean immediate) {
        this.routingKey = checkNotNull(routingKey);
        this.mandatory = mandatory;
        this.immediate = immediate;
    }

    public Publish(String routingKey) {
        this(routingKey, false, false);
    }

    public Publish() {
        this("", false, false);
    }

    public String routingKey() {
        return routingKey;
    }

    public boolean mandatory() {
        return mandatory;
    }

    public boolean immediate() {
        return immediate;
    }

    public static Publish forMessage(Message message, @Nullable Publish publish) {
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
}
