package tc.oc.api.exceptions;

import javax.annotation.Nullable;

import tc.oc.api.message.types.Reply;
import tc.oc.commons.core.util.ArrayUtils;

/**
 * Thrown when an API call has an exceptional response. This abstracts away HTTP
 * status codes so we can use other protocols for the API.
 */
public class ApiException extends Exception {

    private @Nullable StackTraceElement[] originalTrace;
    private @Nullable StackTraceElement[] callSite;
    private final @Nullable Reply reply;

    public ApiException(String message, @Nullable Reply reply) {
        this(message, null, null, reply);
    }

    public ApiException(String message, @Nullable StackTraceElement[] callSite) {
        this(message, null, callSite);
    }

    public ApiException(String message, @Nullable Throwable cause, @Nullable StackTraceElement[] callSite) {
        this(message, cause, callSite, null);
    }

    public ApiException(String message, @Nullable Throwable cause, @Nullable StackTraceElement[] callSite, @Nullable Reply reply) {
        super(message, cause);
        this.reply = reply;
        setCallSite(callSite);
    }

    public @Nullable Reply getReply() {
        return reply;
    }

    public @Nullable StackTraceElement[] getCallSite() {
        return callSite;
    }

    public void setCallSite(@Nullable StackTraceElement[] callSite) {
        this.callSite = callSite;
        if(callSite != null) {
            if(originalTrace == null) {
                originalTrace = getStackTrace();
            }
            setStackTrace(ArrayUtils.append(originalTrace, callSite));
        }
    }
}
