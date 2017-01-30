package tc.oc.api.exceptions;

import javax.annotation.Nullable;

import tc.oc.api.message.types.Reply;

/**
 * An API request is invalid based on the current state of whatever it is manipulating
 */
public class Conflict extends ApiException {
    public Conflict(String message, @Nullable Reply reply) {
        super(message, reply);
    }
}
