package tc.oc.api.message;

import tc.oc.api.exceptions.SerializationException;

public class NoSuchMessageException extends SerializationException {
    public NoSuchMessageException(String message) {
        super(message);
    }
}
