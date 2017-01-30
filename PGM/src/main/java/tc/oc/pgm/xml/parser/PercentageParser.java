package tc.oc.pgm.xml.parser;

import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class PercentageParser extends PrimitiveParser<Double> {

    @Override
    public String readableTypeName() {
        return "percentage";
    }

    @Override
    protected Double parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        try {
            final double n = Double.parseDouble(text.replace("%", "").trim()) / 100D;
            if(n < 0 || n > 100) {
                throw new InvalidXMLException("Percentage must be between 0 and 100", node);
            }
            return n;
        } catch(NumberFormatException e) {
            throw new FormatException();
        }
    }
}
