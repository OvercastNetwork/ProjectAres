package tc.oc.commons.core.util;

import com.google.common.base.Joiner;

/**
 * Commonly used {@link Joiner}s
 */
public class Joiners {
    private Joiners() {}

    public static final Joiner onBlank = Joiner.on("");
    public static final Joiner onComma = Joiner.on(',');
    public static final Joiner onSpace = Joiner.on(' ');
    public static final Joiner onCommaSpace = Joiner.on(", ");
    public static final Joiner onDot = Joiner.on('.');
}
