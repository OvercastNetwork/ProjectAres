package tc.oc.api.exceptions;

/**
 * Thrown when an unknown message type is received from an AMQP queue.
 * This is usually not a problem. It could be a message on the fanout
 * exchange not intended for the server, or a future message type
 * sent during an upgrade.
 */
public class UnknownMessageType extends RuntimeException {
    private final String type;

    public UnknownMessageType(String type) {
        super("Unknown queue message of type '" + type + "'");
        this.type = type;
    }

    public String getMessageType() {
        return type;
    }
}
