package tc.oc.pgm.xml.parser;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import org.jdom2.Element;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

/**
 * A parser of exclusively {@link Element}s. Trying to parse any other
 * {@link Node} will cause an error.
 */
public interface ElementParser<T> extends Parser<T> {

    T parseElement(Element element) throws InvalidXMLException;

    @Override
    default T parseInternal(Node node) throws InvalidXMLException {
        return parseElement(node.asElement());
    }

    static <T> TypeToken<ElementParser<T>> typeOf(TypeToken<T> type) {
        return new TypeToken<ElementParser<T>>(){}.where(new TypeParameter<T>(){}, type);
    }

    static <T> TypeLiteral<ElementParser<T>> typeOf(TypeLiteral<T> type) {
        return Types.toLiteral(typeOf(Types.toToken(type)));
    }
}
