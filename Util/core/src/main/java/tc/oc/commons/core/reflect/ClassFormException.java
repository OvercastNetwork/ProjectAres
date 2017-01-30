package tc.oc.commons.core.reflect;

import javax.annotation.Nullable;

public class ClassFormException extends ElementFormException {
    private final Class<?> klass;

    public ClassFormException(Class<?> klass) {
        this(klass, null, null);
    }

    public ClassFormException(Class<?> klass, String message) {
        this(klass, message, null);
    }

    public ClassFormException(Class<?> klass, Throwable cause) {
        this(klass, null, cause);
    }

    public ClassFormException(Class<?> klass, @Nullable String message, @Nullable Throwable cause) {
        super(makeMessage(klass, message), cause);
        this.klass = klass;
    }

    private static String makeMessage(Class<?> klass, @Nullable String message) {
        String text = "Invalid form for class " + klass.getName();
        if(message != null) {
            text += ": " + message;
        }
        return text;
    }

    public Class<?> getBadClass() {
        return klass;
    }
}
