package tc.oc.pgm.xml;

import org.jdom2.Attribute;
import org.jdom2.Element;

public class UnrecognizedXMLException extends InvalidXMLException {

    public UnrecognizedXMLException(Attribute attribute) {
        this(Node.of(attribute));
    }

    public UnrecognizedXMLException(Element element) {
        this(Node.of(element));
    }

    public UnrecognizedXMLException(Node node) {
        super("Unrecognized " + node.describeType(), node);
    }

    public UnrecognizedXMLException(String context, Attribute attribute) {
        this(context, Node.of(attribute));
    }

    public UnrecognizedXMLException(String context, Element element) {
        this(context, Node.of(element));
    }

    public UnrecognizedXMLException(String context, Node node) {
        super("Unrecognized " + context + " " + node.describeType(), node);
    }
}
