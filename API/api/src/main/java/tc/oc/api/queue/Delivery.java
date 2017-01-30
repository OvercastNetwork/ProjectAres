package tc.oc.api.queue;

import java.io.IOException;
import java.util.logging.Level;

import com.rabbitmq.client.Envelope;

/**
 * Information about the delivery of an incoming message.
 *
 * Also allows delivery to be acknowledged by calling {@link #ack()}.
 */
public class Delivery {
    private final QueueClient client;
    private final String consumerTag;
    private final Envelope envelope;

    public Delivery(QueueClient client, String consumerTag, Envelope envelope) {
        this.client = client;
        this.consumerTag = consumerTag;
        this.envelope = envelope;
    }

    public String consumerTag() {
        return consumerTag;
    }

    public Envelope envelope() {
        return envelope;
    }

    public void ack() {
        try {
            client.getChannel().basicAck(envelope().getDeliveryTag(), false);
        } catch(IOException e) {
            client.getLogger().log(Level.SEVERE, "Failed to ACK delivery " + this, e);
        }
    }
}
