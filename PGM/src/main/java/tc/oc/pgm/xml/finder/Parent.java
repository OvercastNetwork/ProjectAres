package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * The parent {@link Element} (name is ignored)
 */
@Singleton
public class Parent implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element element, String name) {
        return Stream.of(Node.of(element));
    }
}
