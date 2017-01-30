package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * Children with no child elements of their own
 */
@Singleton
public class EmptyChildren implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        return parent.getChildren(name)
                     .stream()
                     .filter(child -> child.getChildren().isEmpty())
                     .map(Node::of);
    }
}
