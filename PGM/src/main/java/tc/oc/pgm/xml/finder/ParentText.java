package tc.oc.pgm.xml.finder;

import java.util.stream.Stream;
import javax.inject.Singleton;

import org.jdom2.Element;
import tc.oc.pgm.xml.Node;

/**
 * The parent {@link Element}, but only if it contains non-whitespace text content
 */
@Singleton
public class ParentText implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        return "".equals(parent.getTextNormalize()) ? Stream.empty()
                                                    : Stream.of(Node.of(parent));
    }
}
