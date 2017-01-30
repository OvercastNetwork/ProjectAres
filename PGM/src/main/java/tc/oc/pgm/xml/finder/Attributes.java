package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Attribute;
import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * {@link Attribute}s of the parent element with a matching name
 */
@Singleton
public class Attributes implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        return Node.attributes(parent, name);
    }
}
