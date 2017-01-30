package tc.oc.parse.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import tc.oc.parse.ParseException;

public interface NodeParser<T> extends ElementParser<T>, DocumentParser<T> {

    T parse(Node node) throws ParseException;

    @Override
    default T parse(Element element) throws ParseException {
        return parse((Node) element);
    }

    @Override
    default T parse(Document document) throws ParseException {
        return parse((Node) document);
    }
}
