package tc.oc.commons.core.util;

/**
 * Indicates that multiple equal elements are present in some context where elements must be unique
 */
public class DuplicateElementException extends RuntimeException {

    public DuplicateElementException() {
    }

    public DuplicateElementException(String message) {
        super(message);
    }

    public DuplicateElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateElementException(Throwable cause) {
        super(cause);
    }
}
