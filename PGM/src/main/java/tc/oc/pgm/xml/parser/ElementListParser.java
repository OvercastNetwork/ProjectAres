package tc.oc.pgm.xml.parser;

import java.util.List;

import org.jdom2.Element;
import tc.oc.pgm.xml.InvalidXMLException;

/**
 * A parser that generates a single {@link T} from a list of {@link Element}s.
 *
 * If a single {@link Element} is passed to {@link #parseElement(Element)},
 * it's list of children will be parsed as a single item.
 */
public interface ElementListParser<T> extends ElementParser<T> {

    T parseElementList(List<Element> elements) throws InvalidXMLException;

    @Override
    default T parseElement(Element element) throws InvalidXMLException {
        return parseElementList(element.getChildren());
    }
}
