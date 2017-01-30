package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class StringParser extends PrimitiveParser<String> {

    private static final StringParser INSTANCE = new StringParser();
    public static StringParser get() { return INSTANCE; }

    @Inject private StringParser() {}

    @Override
    public String parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        return text;
    }
}
