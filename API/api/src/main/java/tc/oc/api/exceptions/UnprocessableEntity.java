package tc.oc.api.exceptions;

import javax.annotation.Nullable;

import tc.oc.api.message.types.Reply;

/**
 * HTTP 422 i.e. validation failed
 */
public class UnprocessableEntity extends ApiException {
    public UnprocessableEntity(String message, @Nullable Reply reply) {
        super(message, reply);
    }
}
