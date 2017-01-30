package tc.oc.api.exceptions;

import javax.annotation.Nullable;

import tc.oc.api.message.types.Reply;

/**
 * An API request was not allowed
 */
public class Forbidden extends ApiException {
    public Forbidden(String message, @Nullable Reply reply) {
        super(message, reply);
    }
}
