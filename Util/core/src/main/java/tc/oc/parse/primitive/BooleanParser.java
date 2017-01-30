package tc.oc.parse.primitive;

import tc.oc.parse.FormatException;
import tc.oc.parse.ParseException;
import tc.oc.parse.Parser;

public class BooleanParser implements Parser<Boolean> {
    @Override
    public Boolean parse(String text) throws ParseException {
        if("true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text) || "on".equalsIgnoreCase(text)) {
            return true;
        }
        if("false".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text) || "off".equalsIgnoreCase(text)) {
            return false;
        }
        throw new FormatException();
    }
}
