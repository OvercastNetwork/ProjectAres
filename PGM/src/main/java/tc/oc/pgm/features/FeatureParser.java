package tc.oc.pgm.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import org.jdom2.Attribute;
import org.jdom2.Element;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.AmbiguousElementException;
import tc.oc.commons.core.util.Ranges;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.UnrecognizedXMLException;
import tc.oc.pgm.xml.parser.ElementParser;
import tc.oc.pgm.xml.parser.Parser;
import tc.oc.pgm.xml.parser.PrimitiveParser;
import tc.oc.pgm.xml.validate.Validation;

import static java.util.stream.Collectors.toList;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.*;
import static tc.oc.commons.core.stream.Collectors.zeroOrOne;

/**
 * Parses references and definitions for feature {@link T}.
 *
 * Other classes that want to parse {@link T}s should inject this class and use the {@link #property}
 * methods (in most cases), or possibly the {@link #parseReference} methods (for irregular reference syntax).
 * They can also just inject {@link ElementParser} or {@link Parser}, which are linked to this class.
 * {@link #parseElement(Element)} and {@link #parse(Node)} will handle references or definitions.
 *
 * This class depends on the {@link FeatureDefinitionParser} for {@link T}.
 * It handles registration of definitions and references with the {@link FeatureDefinitionContext}.
 *
 * A {@link FeatureDefinitionParser} implementation can safely depend on its own {@link FeatureParser}
 * if it needs to recursively parse its own type.
 *
 * A specialized {@link FeatureParser} for {@link T} can be bound using {@link FeatureBinder#bindParser()}.
 * Otherwise, {@link FeatureParser} itself will get a JIT binding, if anything depends on it.
 *
 * @see FeatureDefinitionParser
 *
 * TODO: Extract the property stuff and integrate it with {@link tc.oc.pgm.xml.property.PropertyBuilder}
 */
public class FeatureParser<T extends FeatureDefinition> extends PrimitiveParser<T> implements ElementParser<T> {

    protected final TypeToken<T> featureTypeToken;
    protected final Class<T> featureType;
    protected final String featureName;

    @Inject protected FeatureDefinitionContext features;
    @Inject protected Provider<FeatureDefinitionParser<T>> definitionParser; // Provider to avoid circular deps

    protected FeatureParser() {
        this(null);
    }

    @Inject public FeatureParser(@Nullable TypeLiteral<T> type) {
        this.featureTypeToken = type != null ? Types.toToken(type)
                                             : new TypeToken<T>(getClass()){};
        this.featureType = (Class<T>) featureTypeToken.getRawType();
        this.featureName = FeatureDefinition.getFeatureName(featureType);
    }

    @Override
    public TypeToken<T> paramToken() {
        return featureTypeToken;
    }

    /**
     * Default property name for {@link T}
     */
    public String propertyName() {
        return featureName;
    }

    /**
     * The name of the ID attribute on definitions and references for {@link T}.
     */
    public String idAttributeName() {
        return "id";
    }

    /**
     * A transformation to apply to all parsed IDs before storing them or looking them up.
     */
    public String mangleId(String unmangled) {
        return unmangled;
    }

    /**
     * Parse the given {@link Element} as a {@link T} of some kind (reference or definition).
     */
    @Override
    public T parseElement(Element el) throws InvalidXMLException {
        if(isReference(el)) {
            return parseReferenceElement(el);
        } else {
            return parseDefinition(el);
        }
    }

    @Override
    public T parseInternal(Node node) throws InvalidXMLException {
        if(node.isElement()) {
            return parseElement(node.asElement());
        } else {
            return parseReference(node);
        }
    }

    @Override
    protected T parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        if(node.isElement()) {
            // Assume that we will never need to parse an element with split content
            return parseElement(node.asElement());
        } else {
            return parseReference(node, text);
        }
    }

    /**
     * Can the given element be ignored when parsing child elements?
     *
     * This prevents an "unrecognized element" error from being thrown.
     * It can be used to ignore other valid elements that are mixed in
     * with filters.
     */
    protected boolean canIgnore(Element el) throws InvalidXMLException {
        return false;
    }

    public boolean isDefinition(Element el) throws InvalidXMLException {
        return definitionParser.get().isDefinition(el);
    }

    /**
     * Can the given {@link Element} be parsed as a reference?
     */
    public boolean isReference(Element el) throws InvalidXMLException {
        return el.getContent().isEmpty() &&
               el.getAttributes().size() == 1 &&
               el.getAttributes().get(0).getName().equals(idAttributeName()) &&
               ("ref".equals(el.getName()) || propertyName().equals(el.getName()));
    }

    /**
     * Can the given {@link Element} be parsed as a {@link T} of any kind?
     */
    public boolean isParseable(Element el) throws InvalidXMLException {
        return isReference(el) || isDefinition(el);
    }

    /**
     * Like {@link FeatureParser#isParseable(Element)}, but throws an exception if the
     * element is unrecognized (and fails {@link FeatureParser#canIgnore(Element)}).
     *
     * This should be used to filter out ignored elements, in places
     * where unrecognized elements do not belong.
     */
    public boolean isParseableChild(Element el) throws InvalidXMLException {
        if(isParseable(el)) {
            return true;
        } else if(!canIgnore(el)) {
            throw new UnrecognizedXMLException(propertyName(), el);
        } else {
            return false;
        }
    }

    /**
     * Try to parse an ID applied to the given definition element,
     * or return empty if the definition is anonymous.
     */
    public Optional<String> parseDefinitionId(Element el, T definition) throws InvalidXMLException {
        return Node.tryAttr(el, idAttributeName())
                   .map(Node::getValue);
    }

    /**
     * Do stuff to the given freshly parsed {@link T} definition, and return it.
     *
     * The base method stores it in the {@link FeatureDefinitionContext}.
     */
    public T registerDefinition(Element el, Optional<String> mangledId, T definition) throws InvalidXMLException {
        return features.define(el, mangledId.orElse(null), paramClass(), definition);
    }

    /**
     * Parse the given definition element and register it.
     *
     * If an ID can be parsed, the definition will be registred under that ID.
     */
    public T parseDefinition(Element el) throws InvalidXMLException {
        final T definition = definitionParser.get().parseElement(el);
        return registerDefinition(el, parseDefinitionId(el, definition).map(this::mangleId), definition);
    }

    public T registerReference(Node node, String mangledId) throws InvalidXMLException {
        return features.reference(node, mangledId, paramClass());
    }

    /**
     * Get a {@link T} reference with the given ID from the {@link FeatureDefinitionContext,
     * using the given {@link Node} as the source location.
     *
     * The {@link Node} can be anything, it is only used for error reporting.
     */
    public T parseReference(Node node, String id) throws InvalidXMLException {
        return registerReference(node, mangleId(id));
    }

    /**
     * Call {@link #parseReference(Node, String)} with the node value as the ID.
     */
    public T parseReference(Node node) throws InvalidXMLException {
        return parseReference(node, node.getValueNormalize());
    }

    /**
     * Parse the given {@link Element} as a standalone {@link T} reference.
     */
    public T parseReferenceElement(Element el) throws InvalidXMLException {
        return parseReference(Node.fromRequiredAttr(el, idAttributeName()));
    }

    /**
     * Return all children of the given {@link Element} that pass
     * {@link #isParseableChild(Element)}.
     */
    public Stream<Element> parseableChildren(Element parent) throws InvalidXMLException {
        return parent.getChildren()
                     .stream()
                     .filter(rethrowPredicate(this::isParseableChild));
    }

    /**
     * Parse a single, unique child {@link Element} using {@link #parseElement(Element)}.
     *
     * An exception is thrown if the given element has multiple children, or no children,
     * or a single child that is not parseable.
     */
    public T parseChild(Element parent) throws InvalidXMLException {
        final Optional<T> feature = parseOptionalChild(parent);
        if(feature.isPresent()) return feature.get();
        throw new InvalidXMLException("Missing " + propertyName(), parent);
    }

    /**
     * If the given {@link Element} has a single child element, parse it using {@link #parseElement(Element)}.
     *
     * An exception is thrown if the given element has multiple children.
     */
    public Optional<T> parseOptionalChild(Element parent) throws InvalidXMLException {
        try {
            return parseableChildren(parent).collect(zeroOrOne())
                                            .map(rethrowFunction(this::parseElement));
        } catch(AmbiguousElementException e) {
            throw new InvalidXMLException("Expected a single " + propertyName() + ", not multiple", parent);
        }
    }

    /**
     * Parse all child {@link Element}s of the given parent using {@link #parseElement(Element)},
     * and return them as an ordered {@link Stream}.
     */
    public Stream<T> parseChildren(Element parent) throws InvalidXMLException {
        return parseableChildren(parent).map(rethrowFunction(this::parseElement));
    }

    public List<T> parseChildList(Element parent, Range<Integer> count) throws InvalidXMLException {
        final List<T> list = parseChildren(parent).collect(Collectors.toList());
        if(count.contains(list.size())) return list;

        final Optional<Integer> min = Ranges.minimum(count), max = Ranges.maximum(count);

        if(!max.isPresent()) {
            throw new InvalidXMLException("Expected " + min.get() + " or more child elements", parent);
        } else if(!min.isPresent()) {
            throw new InvalidXMLException("Expected no more than " + max.get() + " child elements", parent);
        } else if(min.equals(max)) {
            throw new InvalidXMLException("Expected exactly " + min.get() + " child elements", parent);
        } else {
            throw new InvalidXMLException("Expected between " + min.get() + " and " + max.get() + " child elements", parent);
        }
    }

    /**
     * Parse all child {@link Element}s of the given parent using {@link #parseElement(Element)},
     * and return them as {@link List} in order of appearance.
     */
    public List<T> parseChildList(Element parent) throws InvalidXMLException {
        return parseChildren(parent).collect(toList());
    }

    public T parseReferenceOrChild(Element parent) throws InvalidXMLException {
        return parseReferenceOrChild(parent, propertyName());
    }

    public T parseReferenceOrChild(Element parent, String name) throws InvalidXMLException {
        return Node.tryAttr(parent, name)
                   .map(rethrowFunction(this::parseReference))
                   .orElseGet(rethrowSupplier(() -> parseChild(parent)));
    }

    public T parseProperty(Element el, String name, String... aliases) throws InvalidXMLException {
        return property(el, name).alias(aliases).required();
    }

    public Optional<T> parseOptionalProperty(Element el, String name, String... aliases) throws InvalidXMLException {
        return property(el, name).alias(aliases).optional();
    }

    public <S extends PropertyBuilder<S>> PropertyBuilder<S> property(Element element) {
        return property(element, propertyName());
    }

    public <S extends PropertyBuilder<S>> PropertyBuilder<S> property(Element element, String name) {
        return new PropertyBuilder<>(element, name);
    }

    public class PropertyBuilder<Self extends PropertyBuilder<Self>> {
        protected final Element element;
        protected final String name;
        protected final Set<String> names = new HashSet<>();
        protected final List<Validation<? super T>> validations = new ArrayList<>();

        public PropertyBuilder(Element element, String name) {
            this.element = element;
            this.name = name;
            this.names.add(name);
        }

        /**
         * Called after each T is parsed
         */
        protected T postParse(T feature, @Nullable Node node) throws InvalidXMLException {
            features.validate(feature, node, validations);
            return feature;
        }

        /**
         * Parse the entire property from the parent element. If there are no property nodes
         * present, Optional.empty() is returned. If a list is returned, that means some
         * nodes are present, though they may be empty.
         */
        protected Optional<List<T>> parseParent() throws InvalidXMLException {
            final ImmutableList.Builder<T> results = ImmutableList.builder();
            boolean present = false;
            if(parseAttributes(results)) present = true;
            if(parseChildren(results)) present = true;
            return present ? Optional.of(results.build()) : Optional.empty();
        }

        protected boolean parseAttributes(ImmutableList.Builder<T> results) throws InvalidXMLException {
            boolean present = false;
            for(Attribute attr : XMLUtils.getAttributes(element, names)) {
                present = true;
                parseAttribute(results, attr);
            }
            return present;
        }

        protected void parseAttribute(ImmutableList.Builder<T> results, Attribute attr) throws InvalidXMLException {
            final Node node = new Node(attr);
            results.add(postParse(parseReference(node), node));
        }

        protected boolean parseChildren(ImmutableList.Builder<T> results) throws InvalidXMLException {
            boolean present = false;
            for(Element child : XMLUtils.getChildren(element, names)) {
                present = true;
                parseChild(results, child);
            }
            return present;
        }

        protected void parseChild(ImmutableList.Builder<T> results, Element child) throws InvalidXMLException {
            final Node node = new Node(child);
            parseableChildren(child).forEach(rethrowConsumer(el -> results.add(postParse(parseElement(el), node))));
        }

        protected Self self() {
            return (Self) this;
        }

        public Self alias(String... aliases) {
            names.addAll(Arrays.asList(aliases));
            return self();
        }

        public Self validate(Validation<? super T> validation) {
            validations.add(validation);
            return self();
        }

        public Optional<List<T>> optionalMulti() throws InvalidXMLException {
            return parseParent();
        }

        public List<T> multi() throws InvalidXMLException {
            return optionalMulti().orElseThrow(() -> new InvalidXMLException("Missing " + featureName + " property '" + name + "'", element));
        }

        public Optional<T> optional() throws InvalidXMLException {
            return optionalMulti().map(rethrowFunction(features -> {
                switch(features.size()) {
                    case 0: throw new InvalidXMLException("Missing " + featureName + " value for '" + name + "' property", element);
                    case 1: return features.get(0);
                    default: throw new InvalidXMLException("Conflicting " + featureName + " values for '" + name + "' property", element);
                }
            }));
        }

        public T optional(@Nullable T def) throws InvalidXMLException {
            return optional().orElse(def);
        }

        public T optionalGet(Supplier<T> def) throws InvalidXMLException {
            return optional().orElseGet(def);
        }

        public T required() throws InvalidXMLException {
            final Optional<T> feature = optional();
            if(feature.isPresent()) return feature.get();
            throw new InvalidXMLException("Missing " + featureName + " property '" + name + "'", element);
        }
    }
}
