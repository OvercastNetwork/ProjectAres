package tc.oc.api.model;

import tc.oc.api.exceptions.SerializationException;

public class NoSuchModelException extends SerializationException {
    public NoSuchModelException(String message) {
        super(message);
    }
}
