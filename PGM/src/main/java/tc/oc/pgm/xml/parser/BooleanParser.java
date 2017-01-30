package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class BooleanParser extends PrimitiveParser<Boolean> {

    private static final BooleanParser INSTANCE = new BooleanParser();
    public static BooleanParser get() { return INSTANCE; }

    @Inject private BooleanParser() {}

    @Override
    public Boolean parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        if("true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text) || "on".equalsIgnoreCase(text)) {
            return true;
        }
        if("false".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text) || "off".equalsIgnoreCase(text)) {
            return false;
        }
        throw new FormatException();
    }
}
