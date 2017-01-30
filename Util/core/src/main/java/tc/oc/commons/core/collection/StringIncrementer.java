package tc.oc.commons.core.collection;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Appends or increments a numeric suffix to a string e.g.
 *
 * {@code
 *     StringIncrementer si = new StringIncrementer("-", 1);
 *     si.apply("woot")         // returns "woot-1"
 *     si.apply("woot-1")       // returns "woot-2"
 *     si.apply("woot-27")      // returns "woot-28"
 * }
 */
public class StringIncrementer implements UnaryOperator<String> {

    private final String delimiter;
    private final int initial;
    private final Pattern pattern;

    public StringIncrementer(String delimiter) {
        this(delimiter, 0);
    }

    public StringIncrementer(String delimiter, int initial) {
        this.delimiter = delimiter;
        this.initial = initial;
        this.pattern = Pattern.compile("^(.*)" + Pattern.quote(delimiter) + "(\\d+)$");
    }

    @Override
    public String apply(String s) {
        final Matcher matcher = pattern.matcher(s);
        if(matcher.matches()) {
            return matcher.group(1) + delimiter + (Integer.parseInt(matcher.group(2)) + 1);
        } else {
            return s + delimiter + initial;
        }
    }
}
