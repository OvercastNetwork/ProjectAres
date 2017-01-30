package tc.oc.pgm.xml.parser;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import org.jdom2.Element;
import tc.oc.commons.core.ListUtils;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.AmbiguousElementException;
import tc.oc.commons.core.util.DuplicateElementException;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.NodeSplitter;
import tc.oc.pgm.xml.finder.Attributes;
import tc.oc.pgm.xml.finder.EmptyChildren;
import tc.oc.pgm.xml.finder.Grandchildren;
import tc.oc.pgm.xml.finder.NodeFinder;
import tc.oc.pgm.xml.validate.Validatable;
import tc.oc.pgm.xml.validate.Validation;
import tc.oc.pgm.xml.validate.ValidationContext;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

/**
 * Parses a named property from an {@link Element}.
 *
 * The inner type {@link I} can be anything. The provided {@link Parser}
 * is used to parse instances of this type.
 *
 * The outer type {@link O} is what the parser returns. It can be the same as
 * the inner type, or some container of the inner type. The provided {@link Aggregator}
 * is used to construct the outer type.
 */
public class PropertyParser<O, I> {

    public static final List<Class<? extends NodeFinder>> DEFAULT_NODE_FINDERS = ImmutableList.of(
        Attributes.class, EmptyChildren.class, Grandchildren.class
    );

    private final Provider<? extends Parser<I>> innerParser;
    private final Provider<ValidationContext> validationContext;

    private final String name;
    private final List<String> aliases;
    private final Set<String> names;
    private final List<Provider<? extends NodeFinder>> finders;
    private final Provider<? extends NodeSplitter> splitter;
    private final Aggregator<O, I> aggregator;
    private final List<Provider<Validation<? super I>>> validations;

    /**
     * @param binder         Required so that this class can request injection for itself.
     *                       (we need to instantiate this at configuration time, so we can't expect to have an Injector)
     * @param name           Name of the property (only used for error messages, does not affect parsing)
     * @param aliases
     * @param finders        Used to find the {@link Node}s that {@link I} instances will be parsed from
     * @param splitter       Used to split the value of each {@link Node}s into multiple strings that are
     *                       parsed as {@link I} instances (this only works with a {@link PrimitiveParser})
     * @param aggregator     Used to combine {@link I} instances into an {@link O} instance
     * @param validations    Validations to run on each {@link I} instance
     */
    public PropertyParser(Binder binder,
                          String name,
                          Iterable<String> aliases,
                          List<Class<? extends NodeFinder>> finders,
                          Class<? extends NodeSplitter> splitter,
                          Aggregator<O, I> aggregator,
                          List<Class<Validation<? super I>>> validations) {

        Types.assertFullySpecified(aggregator.outerTypeToken());
        Types.assertFullySpecified(aggregator.innerTypeToken());

        this.name = name;
        this.aliases = ImmutableList.copyOf(aliases);
        this.names = ImmutableSet.<String>builder().add(name).addAll(aliases).build();
        this.aggregator = aggregator;

        this.finders = ListUtils.transformedCopyOf(finders, binder::getProvider);
        this.splitter = binder.getProvider(splitter);
        this.validations = ListUtils.transformedCopyOf(validations, binder::getProvider);

        // Figure out which base parser type we need
        final Class<? extends Parser> parserBase;
        if(Types.isAssignable(FeatureDefinition.class, aggregator.innerTypeToken())) {
            // If I is a FeatureDefinition, get a FeatureParser so we can parse references
            parserBase = FeatureParser.class;
        } else if(!NodeSplitter.Atom.class.isAssignableFrom(splitter)) {
            // If the splitter is anything besides Atom, we will need a PrimitiveParser
            parserBase = PrimitiveParser.class;
        } else {
            // Otherwise, get a Parser<I>
            parserBase = Parser.class;
        }

        this.innerParser = binder.getProvider(Key.get(Types.parameterizedTypeLiteral(parserBase, aggregator.innerTypeLiteral())));
        this.validationContext = binder.getProvider(ValidationContext.class);
    }

    public String name() {
        return name;
    }

    public List<String> aliases() {
        return aliases;
    }

    public Set<String> names() {
        return names;
    }

    public Stream<Node> findNodes(Element parent) throws InvalidXMLException {
        return finders.stream().map(Provider::get).flatMap(
            finder -> names.stream().flatMap(
                name -> finder.findNodes(parent, name)
            )
        );
    }

    private I validate(I value, Node node) throws InvalidXMLException {
        final ValidationContext validationContext = this.validationContext.get();
        if(value instanceof Validatable) {
            validationContext.validate(((Validatable) value).offeringNode(node));
        }
        validations.forEach(rethrowConsumer(validation -> validationContext.validate(validation.get().bind(value, node))));
        return value;
    }

    public O parse(Element parent, Stream<Node> nodes) throws InvalidXMLException {
        final Parser<I> innerParser = this.innerParser.get();
        final Stream<I> values;

        if(innerParser instanceof PrimitiveParser) {
            // For PrimitiveParsers, split the text of attributes and children
            final PrimitiveParser<I> primitiveParser = (PrimitiveParser<I>) innerParser;
            final NodeSplitter splitter = this.splitter.get();
            values = nodes.flatMap(node -> splitter.split(node).map(rethrowFunction(
                text -> validate(primitiveParser.parse(node, text), node)
            )));
        } else {
            values = nodes.map(rethrowFunction(
                node -> validate(innerParser.parse(node), node)
            ));
        }

        try {
            return aggregator.aggregateElements(values);
        } catch(NoSuchElementException e) {
            throw new InvalidXMLException("Missing value for property '" + name() + "'", parent);
        } catch(AmbiguousElementException e) {
            throw new InvalidXMLException("Multiple values for property '" + name() + "'", parent);
        } catch(DuplicateElementException e) {
            throw new InvalidXMLException("Duplicate values for property '" + name() + "'", parent);
        }
    }
}
