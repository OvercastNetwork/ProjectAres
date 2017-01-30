package tc.oc.parse.xml;

import org.w3c.dom.Node;
import tc.oc.parse.FormatException;

public class UnrecognizedNodeException extends FormatException {
    public UnrecognizedNodeException(Node node) {
        super("Unexpected node"); // TODO: better message
    }
}
