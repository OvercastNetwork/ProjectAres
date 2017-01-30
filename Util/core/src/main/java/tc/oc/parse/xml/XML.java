package tc.oc.parse.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tc.oc.commons.core.util.Streams;
import tc.oc.parse.MissingException;
import tc.oc.parse.ParseException;

public class XML {

    public static Stream<Node> nodes(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
                        .mapToObj(nodeList::item);
    }

    public static Stream<Element> elements(NodeList nodeList) {
        return Streams.instancesOf(IntStream.range(0, nodeList.getLength())
                                            .mapToObj(nodeList::item),
                                   Element.class);
    }

    public static Stream<Node> childNodes(Node parent) {
        return nodes(parent.getChildNodes());
    }

    public static Stream<Element> children(Element parent) {
        return elements(parent.getChildNodes());
    }

    public static Stream<Element> childrenNamed(Element parent, String... names) {
        return childrenNamed(parent, Arrays.asList(names));
    }

    public static Stream<Element> childrenNamed(Element parent, Collection<String> names) {
        switch(names.size()) {
            case 0: return Stream.empty();
            case 1: return elements(parent.getElementsByTagName(names.iterator().next()));
            default: return children(parent).filter(element -> names.contains(element.getTagName()));
        }
    }

    public static Optional<Attr> attr(Element parent, String name) {
        return Optional.ofNullable(parent.getAttributeNode(name));
    }

    public static Optional<String> attrValue(Element parent, String name) {
        return attr(parent, name).map(Attr::getValue);
    }

    public static Attr requireAttr(Element parent, String name) throws ParseException {
        final Attr attr = parent.getAttributeNode(name);
        if(attr == null) {
            throw new MissingException("attribute", name);
        }
        return attr;
    }
}
