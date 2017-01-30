package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class DurationParser extends TransfiniteParser<Duration> {

    private static final DurationParser INSTANCE = new DurationParser();
    public static DurationParser get() { return INSTANCE; }

    @Inject private DurationParser() {}

    @Override
    protected Duration infinity(boolean sign) {
        if(sign) return TimeUtils.INF_POSITIVE;
        throw new UnsupportedOperationException();
    }

    @Override
    protected Duration parseFinite(Node node, String text) throws FormatException, InvalidXMLException {
        try {
            return TimeUtils.parseDuration(text);
        } catch(DateTimeParseException e) {
            throw new FormatException();
        }
    }
}
