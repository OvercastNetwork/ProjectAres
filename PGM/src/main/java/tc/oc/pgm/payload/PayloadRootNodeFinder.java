package tc.oc.pgm.payload;

import org.jdom2.Element;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.finder.NodeFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class PayloadRootNodeFinder implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        final List<Element> elements = new ArrayList<>();
        elements.addAll(XMLUtils.flattenElements(parent, "payloads", "payload"));
        return elements.stream().map(Node::of);
    }
}
