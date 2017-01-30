package tc.oc.parse.xml;

import org.w3c.dom.Element;
import tc.oc.commons.core.reflect.AutoReified;
import tc.oc.parse.ParseException;

public interface ElementParser<T> extends AutoReified<T> {
    T parse(Element element) throws ParseException;
}
