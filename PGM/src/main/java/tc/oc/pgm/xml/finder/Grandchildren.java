package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * Children of child {@link Element}s of the parent with a matching name
 */
@Singleton
public class Grandchildren implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        return parent.getChildren(name)
                     .stream()
                     .flatMap(Node::elements);
    }
}
