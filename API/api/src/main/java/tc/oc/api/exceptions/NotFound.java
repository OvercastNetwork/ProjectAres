package tc.oc.api.exceptions;

import javax.annotation.Nullable;

import tc.oc.api.message.types.Reply;

/**
 * Something was referenced through the API that does not exist
 */
public class NotFound extends ApiException {

    public NotFound() {
        this(null);
    }

    public NotFound(@Nullable String message) {
        this(message, null);
    }

    public NotFound(@Nullable String message, @Nullable Reply reply) {
        super(message, reply);
    }
}
