package tc.oc.pgm.xml;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.LocatedJDOMFactory;

public class BoundedJDOMFactory extends LocatedJDOMFactory {
    @Override
    public Element element(int line, int col, String name, Namespace namespace) {
        return new BoundedElement(name, namespace);
    }

    @Override
    public Element element(int line, int col, String name) {
        return new BoundedElement(name);
    }

    @Override
    public Element element(int line, int col, String name, String uri) {
        return new BoundedElement(name, uri);
    }

    @Override
    public Element element(int line, int col, String name, String prefix, String uri) {
        return new BoundedElement(name, prefix, uri);
    }
}
