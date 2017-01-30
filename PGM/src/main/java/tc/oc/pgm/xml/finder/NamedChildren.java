package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * Child {@link Element}s with a matching name
 */
@Singleton
public class NamedChildren implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        return Node.elements(parent, name);
    }
}
