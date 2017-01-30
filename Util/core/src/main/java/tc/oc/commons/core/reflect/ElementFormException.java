package tc.oc.commons.core.reflect;

/**
 * Form of some language element (class, method, etc) does not meet some requirement
 */
public abstract class ElementFormException extends Error {
    protected ElementFormException(String message, Throwable cause) {
        super(message, cause);
    }
}
