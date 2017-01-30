package tc.oc.parse.xml;

import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import org.w3c.dom.Node;
import tc.oc.parse.ParseException;
import tc.oc.parse.Parser;

/**
 * A {@link NodeParser<T>} that applies {@link Parser<T>} to the result of {@link Node#getTextContent()}
 */
public class PrimitiveNodeParser<T> implements NodeParser<T> {

    private final Parser<T> parser;

    @Inject PrimitiveNodeParser(Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public TypeToken<T> paramToken() {
        return parser.paramToken();
    }

    @Override
    public T parse(Node node) throws ParseException {
        return parser.parse(node.getTextContent());
    }
}
