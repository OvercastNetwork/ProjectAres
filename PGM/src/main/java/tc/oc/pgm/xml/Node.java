package tc.oc.pgm.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Parent;
import org.jdom2.located.Located;
import tc.oc.commons.core.util.ArrayUtils;
import tc.oc.commons.core.util.Optionals;

/**
 * A hybrid wrapper for either an {@link Element} or an {@link Attribute},
 * enabling both of them to be handled in a generic way.
 */
public class Node {
    private final Object node;

    public Node(Element element) {
        Preconditions.checkNotNull(element);
        Preconditions.checkArgument(element instanceof BoundedElement);
        this.node = element;
    }

    public Node(Attribute attribute) {
        Preconditions.checkNotNull(attribute);
        this.node = attribute;
    }

    public static Node of(Element element) {
        return new Node(element);
    }

    public static Node of(Attribute attribute) {
        return new Node(attribute);
    }

    private static boolean equals(Parent a, Parent b) {
        if(a == b) return true;
        if(a instanceof Element && b instanceof Element) {
            return equals((Element) a, (Element) b);
        }
        return a.equals(b);
    }

    private static boolean equals(Element a, Element b) {
        if(a == null || b == null) return false;
        if(a == b) return true;
        if(!equals(a.getParent(), b.getParent())) return false;
        return a.getParent().indexOf(a) == b.getParent().indexOf(b);
    }

    private static boolean equals(Attribute a, Attribute b) {
        if(a == null || b == null) return false;
        if(a == b) return true;
        if(!a.getName().equals(b.getName())) return false;
        return equals(a.getParent(), b.getParent());
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof Node)) return false;

        final Node that = (Node) obj;
        if(this.node instanceof Attribute && that.node instanceof Attribute) {
            return equals((Attribute) this.node, (Attribute) that.node);
        } else if(this.node instanceof Element && that.node instanceof Element) {
            return equals((Element) this.node, (Element) that.node);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    public String getName() {
        if(this.node instanceof Attribute) {
            return ((Attribute) this.node).getName();
        } else {
            return ((Element) this.node).getName();
        }
    }

    /**
     * Gets the exact text content of the element or attribute
     */
    public String getValue() {
        if(this.node instanceof Attribute) {
            return ((Attribute) this.node).getValue();
        } else {
            return ((Element) this.node).getText();
        }
    }

    /**
     * If this Node is wrapping an Attribute, returns the same as {@link #getValue()}.
     * If this Node is wrapping an Element, returns {@link Element#getTextNormalize()}.
     */
    public String getValueNormalize() {
        if(this.node instanceof Attribute) {
            return ((Attribute) this.node).getValue();
        } else {
            return ((Element) this.node).getTextNormalize();
        }
    }

    public boolean isAttribute() {
        return this.node instanceof Attribute;
    }

    public boolean isElement() {
        return this.node instanceof Element;
    }

    public Attribute asAttribute() {
        return asType(Attribute.class);
    }

    public Element asElement() {
        return asType(Element.class);
    }

    public Stream<Node> attributes() {
        return node instanceof Element
               ? ((Element) node).getAttributes().stream().map(Node::of)
               : Stream.empty();
    }

    public Stream<Node> elements() {
        return node instanceof Element
               ? ((Element) node).getChildren().stream().map(Node::of)
               : Stream.empty();
    }

    public boolean hasNodes() {
        if(!(node instanceof Element)) return false;
        final Element element = (Element) node;
        return !(element.getAttributes().isEmpty() &&
                 element.getChildren().isEmpty());
    }

    public Stream<Node> nodes() {
        return Stream.concat(attributes(), elements());
    }

    private <T> T asType(Class<T> type) {
        if(type.isInstance(node)) {
            return type.cast(node);
        }
        throw new Error("Node is not a " + type.getSimpleName() + ": " + describeWithLocation());
    }

    public Optional<Attribute> tryAttribute() {
        return Optionals.cast(node, Attribute.class);
    }

    public Optional<Element> tryElement() {
        return Optionals.cast(node, Element.class);
    }

    public @Nullable Document getDocument() {
        if(this.node instanceof Attribute) {
            return ((Attribute) this.node).getDocument();
        } else {
            return ((Element) this.node).getDocument();
        }
    }

    public Optional<Document> document() {
        return Optional.ofNullable(getDocument());
    }

    public Optional<String> documentUri() {
        return document().map(Document::getBaseURI);
    }

    public String describeType() {
        if(node instanceof Element) return "element";
        if(node instanceof Attribute) return "attribute";
        return node.getClass().getSimpleName();
    }

    private static String describe(Element el) {
        return "'" + el.getName() + "' element";
    }

    public String describe() {
        if(node instanceof Element) {
            return describe((Element) node);
        } else {
            Attribute attr = (Attribute) node;
            return "'" + attr.getName() + "' attribute of " + describe(attr.getParent());
        }
    }

    public static int startLine(Object node) {
        if(node instanceof BoundedElement) {
            return ((BoundedElement) node).getStartLine();
        } else if(node instanceof Located) {
            return ((Located) node).getLine();
        } else if(node instanceof Attribute) {
            return startLine(((Attribute) node).getParent());
        } else {
            return 0;
        }
    }

    public int startLine() {
        return startLine(node);
    }

    public int endLine() {
        if(node instanceof BoundedElement) {
            return ((BoundedElement) node).getEndLine();
        } else if(node instanceof Located) {
            return ((Located) node).getLine();
        } else if(node instanceof Attribute) {
            return startLine(((Attribute) node).getParent());
        } else {
            return 0;
        }
    }

    public int column() {
        if(node instanceof Located) {
            return ((Located) node).getColumn();
        } else {
            return 0;
        }
    }

    public static Optional<String> describeLocation(int startLine, int endLine, int column) {
        if(startLine > 0) {
            if(endLine > 0 && endLine != startLine) {
                return Optional.of("line " + startLine + " to " + endLine);
            }

            if(column > 0) {
                return Optional.of("line " + startLine + ", column " + column);
            }

            return Optional.of("line " + startLine);
        }

        return Optional.empty();
    }

    public Optional<String> describeLocation() {
        return describeLocation(startLine(), endLine(), column());
    }

    public String describeWithLocation() {
        return Optionals.reduce(describe(), describeLocation(), (d, l) -> d + " @ " + l);
    }

    public String describeWithDocumentAndLocation() {
        return Optionals.reduce(describeWithLocation(), documentUri(), (loc, doc) -> doc + " - " + loc);
    }

    @Override
    public String toString() {
        return describeWithLocation();
    }

    private static Node wrapUnique(Node prev, boolean unique, String name, Object thing) throws InvalidXMLException {
        if(thing == null) return prev;
        Node node = thing instanceof Element ? new Node((Element) thing) : new Node((Attribute) thing);
        if(unique && prev != null) throw new InvalidXMLException("Multiple values for '" + name + "'", node);
        return node;
    }

    /**
     * Return a new Node wrapping an Attribute of the given Element matching one of
     * the given names, or null if the given Element has no matching Attributes.
     */
    public static @Nullable Node fromAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return fromAttr(el, name, ImmutableSet.copyOf(aliases));
    }

    public static @Nullable Node fromAttr(Element el, String name, Set<String> aliases) throws InvalidXMLException {
        Node node = null;
        for(String alias : Sets.union(ImmutableSet.of(name), aliases)) {
            node = wrapUnique(node, true, alias, el.getAttribute(alias));
        }
        return node;
    }

    public static Optional<Node> tryAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return Optional.ofNullable(fromAttr(el, name, aliases));
    }

    /**
     * Return a new Node wrapping the named Attribute of the given Element.
     * If the Attribute does not exist, throw an InvalidXMLException complaining about it.
     */
    public static Node fromRequiredAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        Node node = fromAttr(el, name, aliases);
        if(node == null) {
            throw new InvalidXMLException("attribute '" + name + "' is required", el);
        }
        return node;
    }

    public static Stream<Node> attributes(Element el) {
        return el.getAttributes().stream().map(Node::of);
    }

    public static Stream<Node> attributes(Element el, String name, String... aliases) {
        return attributes(el, ImmutableSet.<String>builder().add(name).add(aliases).build());
    }

    public static Stream<Node> attributes(Element el, Set<String> names) {
        return el.getAttributes()
                 .stream()
                 .filter(attr -> names.contains(attr.getName()))
                 .map(Node::of);
    }

    public static Stream<Node> elements(Element parent) {
        return parent.getChildren().stream().map(Node::of);
    }

    public static Stream<Node> elements(Element parent, String name, String... aliases) {
        return elements(parent, ImmutableSet.<String>builder().add(name).add(aliases).build());
    }

    public static Stream<Node> elements(Element parent, Set<String> names) {
        return parent.getChildren()
                     .stream()
                     .filter(el -> names.contains(el.getName()))
                     .map(Node::of);
    }

    public static Stream<Node> nodes(Element parent) {
        return Stream.concat(attributes(parent), elements(parent));
    }

    public static Stream<Node> nodes(Node parent) {
        return parent.isElement() ? nodes(parent.asElement()) : Stream.empty();
    }

    public static Stream<Node> nodes(Element parent, Set<String> names) {
        return Stream.concat(attributes(parent, names), elements(parent, names));
    }

    public static List<Node> fromAttrs(Element el) throws InvalidXMLException {
        return Lists.transform(el.getAttributes(), Node::of);
    }

    public static List<Node> fromChildren(List<Node> nodes, Element el, String name, String... aliases) throws InvalidXMLException {
        aliases = ArrayUtils.append(aliases, name);
        for(Element child : el.getChildren()) {
            if(ArrayUtils.contains(aliases, child.getName())) {
                nodes.add(Node.of(child));
            }
        }
        return nodes;
    }

    public static List<Node> fromChildren(Element el, String name, String... aliases) throws InvalidXMLException {
        return fromChildren(new ArrayList<>(), el, name, aliases);
    }

    public static @Nullable Node fromNullable(Element el) {
        return el == null ? null : Node.of(el);
    }

    public static @Nullable Node fromNullable(Attribute attr) {
        return attr == null ? null : Node.of(attr);
    }

    public static @Nullable Node fromChildOrAttr(Element el, boolean unique, boolean required, String name, String... aliases) throws InvalidXMLException {
        aliases = ArrayUtils.append(aliases, name);
        Node node = null;
        for(String alias : aliases) {
            node = wrapUnique(node, unique, alias, el.getAttribute(alias));
            for(Element child : el.getChildren(alias)) {
                node = wrapUnique(node, unique, alias, child);
            }
        }
        if(required && node == null) {
            throw new InvalidXMLException("attribute or child element '" + name + "' is required", el);
        }
        return node;
    }

    public static @Nullable Node fromChildOrAttr(Element el, boolean unique, String name, String... aliases) throws InvalidXMLException {
        return fromChildOrAttr(el, unique, false, name, aliases);
    }

    public static @Nullable Node fromChildOrAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return fromChildOrAttr(el, true, name, aliases);
    }

    public static @Nullable Node fromLastChildOrAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return fromChildOrAttr(el, false, false, name, aliases);
    }

    public static Node fromRequiredChildOrAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return fromChildOrAttr(el, true, true, name, aliases);
    }

    public static Node fromRequiredLastChildOrAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return fromChildOrAttr(el, false, true, name, aliases);
    }

    public static Optional<Node> childOrAttr(Element el, String name, String... aliases) throws InvalidXMLException {
        return Optional.ofNullable(fromChildOrAttr(el, name, aliases));
    }
}
