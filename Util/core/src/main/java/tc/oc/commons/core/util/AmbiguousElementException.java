package tc.oc.commons.core.util;

/**
 * Indicates that a requested single element is not unique
 */
public class AmbiguousElementException extends RuntimeException {

    public AmbiguousElementException() {
    }

    public AmbiguousElementException(String message) {
        super(message);
    }

    public AmbiguousElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousElementException(Throwable cause) {
        super(cause);
    }
}
