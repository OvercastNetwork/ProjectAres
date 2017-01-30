package tc.oc.pgm.compose;

import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.Range;
import org.jdom2.Element;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ElementParser;
import tc.oc.pgm.xml.parser.ElementListParser;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

/**
 * Parses a {@link Composition<T>} using an injected {@link ElementParser<T>}
 */
public class CompositionParser<T> implements ElementListParser<Composition<T>> {

    @Inject private ElementParser<T> elementParser;
    @Inject private PropertyBuilderFactory<Boolean, ?> booleans;
    @Inject private PropertyBuilderFactory<Double, ?> doubles;
    @Inject private PropertyBuilderFactory<Range<Integer>, ?> integerRanges;
    @Inject private FilterParser filterParser;

    @Override
    public Composition<T> parseElementList(List<Element> elements) throws InvalidXMLException {
        switch(elements.size()) {
            case 0: return new None<>();
            case 1: return parseAtom(elements.get(0));
            default: return new All<>(elements.stream().map(rethrowFunction(this::parseAtom)));
        }
    }

    public Composition<T> parseAtom(Element element) throws InvalidXMLException {
        switch(element.getName()) {
            case "none":
                return new None<>();

            case "maybe":
                return new Maybe<>(filterParser.property(element).required(),
                                   parseElement(element));
            case "all":
                return parseElement(element);

            case "any":
                return new Any<>(integerRanges.property(element, "count")
                                              .optional(Range.singleton(1)),
                                 booleans.property(element, "unique")
                                         .optional(true),
                                 element.getChildren()
                                        .stream()
                                        .map(rethrowFunction(this::parseOption)));
            default:
                return new Unit<>(elementParser.parseElement(element));
        }
    }

    Any.Option<T> parseOption(Element element) throws InvalidXMLException {
        if("option".equals(element.getName())) {
            return new Any.Option<>(doubles.property(element, "weight").optional(1D),
                                    filterParser.property(element).optional(StaticFilter.ALLOW),
                                    parseElement(element));
        } else {
            return new Any.Option<>(1, StaticFilter.ALLOW, parseAtom(element));
        }
    }
}
