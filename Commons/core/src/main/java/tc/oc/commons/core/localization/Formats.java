package tc.oc.commons.core.localization;

import java.text.MessageFormat;
import java.util.Locale;

public class Formats {

    /**
     * Return a {@link MessageFormat} with the given text content,
     * properly escaping single quote characters by doubling them.
     *
     * Since this effectively disables the quoting mechanism, it is
     * impossible for the text to contain something that looks like a
     * placeholder e.g. "{0}" but this is not a problem in practice.
     */
    public static MessageFormat quotedMessage(String text, Locale locale) {
        return new MessageFormat(text.replace("'", "''"), locale);
    }
}
