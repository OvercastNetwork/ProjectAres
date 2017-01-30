package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * All child {@link Element}s of any name
 */
@Singleton
public class AllChildren implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        return Node.elements(parent);
    }
}
