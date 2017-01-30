package tc.oc.pgm.controlpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jdom2.Element;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.finder.NodeFinder;

class ControlPointRootNodeFinder implements NodeFinder {
    @Override
    public Stream<Node> findNodes(Element parent, String name) {
        final List<Element> elements = new ArrayList<>();
        elements.addAll(XMLUtils.flattenElements(parent, "control-points", "control-point"));
        for(Element kingEl : parent.getChildren("king")) {
            elements.addAll(XMLUtils.flattenElements(kingEl, "hills", "hill"));
        }
        return elements.stream().map(Node::of);
    }
}
