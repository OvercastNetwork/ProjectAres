package tc.oc.parse.primitive;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import tc.oc.commons.core.util.TimeUtils;
import tc.oc.parse.FormatException;
import tc.oc.parse.ParseException;
import tc.oc.parse.Parser;

public class DurationParser implements Parser<Duration> {

    @Override
    public Duration parse(String text) throws ParseException {
        try {
            return TimeUtils.parseDuration(text);
        } catch(DateTimeParseException e) {
            throw new FormatException();
        }
    }
}
