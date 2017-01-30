package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * Given a name and parent {@link Element}, return a sequence of {@link Node}s
 * with some structural relationship to the parent.
 */
@Singleton
public interface NodeFinder {

    Stream<Node> findNodes(Element parent, String name);

    NodeFinder EMPTY = (parent, name) -> Stream.empty();
    static NodeFinder empty() {
        return EMPTY;
    }

    static NodeFinder concat(NodeFinder a, NodeFinder b) {
        return (parent, name) -> Stream.concat(a.findNodes(parent, name),
                                               b.findNodes(parent, name));
    }
}
