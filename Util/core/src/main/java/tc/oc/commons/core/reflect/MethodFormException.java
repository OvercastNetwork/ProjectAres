package tc.oc.commons.core.reflect;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

/**
 * Signature of a method does not meet some requirement
 */
public class MethodFormException extends ElementFormException {

    private final Method method;

    public MethodFormException(Method method) {
        this(method, null, null);
    }

    public MethodFormException(Method method, String message) {
        this(method, message, null);
    }

    public MethodFormException(Method method, Throwable cause) {
        this(method, null, cause);
    }

    public MethodFormException(Method method, @Nullable String message, @Nullable Throwable cause) {
        super(makeMessage(method, message), cause);
        this.method = method;
    }

    private static String makeMessage(Method method, @Nullable String message) {
        String text = "Invalid form for method " + method.getDeclaringClass().getName() + "#" + method.getName();
        if(message != null) {
            text += ": " + message;
        }
        return text;
    }

    public Method getMethod() {
        return method;
    }
}
