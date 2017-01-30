package tc.oc.pgm.xml.parser;

import org.bukkit.attribute.Attribute;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class AttributeParser extends PrimitiveParser<Attribute> {
    @Override
    protected Attribute parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        Attribute attribute = Attribute.byName(text);
        if(attribute != null) return attribute;

        attribute = Attribute.byName("generic." + text);
        if(attribute != null) return attribute;

        throw new InvalidXMLException("Unknown attribute '" + text + "'", node);
    }
}
