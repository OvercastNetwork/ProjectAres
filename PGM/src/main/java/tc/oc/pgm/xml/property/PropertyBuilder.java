package tc.oc.pgm.xml.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import org.jdom2.Element;
import tc.oc.commons.core.util.Optionals;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.NodeSplitter;
import tc.oc.pgm.xml.parser.PrimitiveParser;
import tc.oc.pgm.xml.validate.Validation;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

public class PropertyBuilder<T, Self extends PropertyBuilder<T, Self>> {

    protected final Element parent;
    protected final Optional<String> name;
    protected final Set<String> aliases = new HashSet<>();
    protected NodeSplitter splitter = NodeSplitter.ATOM;
    protected final PrimitiveParser<? extends T> parser;
    protected final List<Validation<? super T>> validations = new ArrayList<>();
    protected boolean attributes = true;
    protected boolean elements = true;

    @Inject public PropertyBuilder(@Assisted Element parent, @Assisted String name, PrimitiveParser<T> parser) {
        this(parent, Optional.of(name), parser);
    }

    private PropertyBuilder(Element node, Optional<String> name, PrimitiveParser<T> parser) {
        this.parent = node;
        this.name = name;
        this.parser = parser;
    }

    public Self alias(String... aliases) {
        return alias(Arrays.asList(aliases));
    }

    public Self alias(Collection<String> aliases) {
        this.aliases.addAll(aliases);
        return self();
    }

    public Self validate(Validation<? super T> validation) {
        validations.add(validation);
        return self();
    }

    public Self split(NodeSplitter splitter) {
        this.splitter = splitter;
        return self();
    }

    public Self attributes(boolean attributes) {
        this.attributes = attributes;
        return self();
    }

    public Self elements(boolean elements) {
        this.elements = elements;
        return self();
    }

    public Optional<T> optional() throws InvalidXMLException {
        final List<Pair<Node, String>> nodes = splitNodes().collect(Collectors.toList());
        switch(nodes.size()) {
            case 0: return Optional.empty();
            case 1: return Optional.of(parseAndValidate(nodes.get(0).first, nodes.get(0).second));
            default:
                throw new InvalidXMLException("Multiple values given for unique property '" + name + "'", parent);
        }
    }

    public T optional(T def) throws InvalidXMLException {
        return optional().orElse(def);
    }

    public T required() throws InvalidXMLException {
        return optional().orElseThrow(() -> new InvalidXMLException("Missing required property '" + name + "'", parent));
    }

    public Stream<T> multi() throws InvalidXMLException {
        return splitNodes().map(rethrowFunction(node -> parseAndValidate(node.first, node.second)));
    }

    protected Self self() {
        return (Self) this;
    }

    protected T parseAndValidate(Node node, String text) throws InvalidXMLException {
        final T value = parser.parse(node, text);
        validations.forEach(rethrowConsumer(v -> v.validate(value, node)));
        return value;
    }

    protected Stream<String> splitNode(Node node) throws InvalidXMLException {
        return splitter.split(node);
    }

    protected Stream<Node> findNodes() {
        final Set<String> names = Sets.union(Optionals.toSet(this.name), aliases);
        if(names.isEmpty()) {
            return Stream.of(Node.of(parent));
        }

        Stream<Node> nodes = Stream.empty();
        if(attributes) {
            nodes = Stream.concat(nodes, Node.attributes(parent, names));
        }
        if(elements) {
            nodes = Stream.concat(nodes, Node.elements(parent, names));
        }
        return nodes;
    }

    protected Stream<Pair<Node, String>> splitNodes() {
        return findNodes().flatMap(rethrowFunction(node -> splitNode(node).map(text -> Pair.of(node, text))));
    }
}
