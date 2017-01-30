package tc.oc.parse.xml;

import org.w3c.dom.Document;
import tc.oc.commons.core.reflect.AutoReified;
import tc.oc.parse.ParseException;

public interface DocumentParser<T> extends AutoReified<T> {
    T parse(Document document) throws ParseException;
}
