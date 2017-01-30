package tc.oc.pgm.features;

import org.jdom2.Element;
import tc.oc.commons.core.util.ArrayUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ElementParser;

/**
 * Parses definitions of feature {@link T}
 *
 * Implementations should not do anything special beyond creating instances of {@link T}.
 * {@link FeatureParser} handles ID parsing, registration, references, and property building.
 *
 * External code that wants to parse {@link T}s should use {@link FeatureParser}, and not this interface.
 *
 * @see FeatureParser
 */
public interface FeatureDefinitionParser<T extends FeatureDefinition> extends ElementParser<T> {

    /**
     * Can the given {@link Element} be parsed as a definition? (not a reference)
     */
    default boolean isDefinition(Element el) throws InvalidXMLException {
        return ArrayUtils.contains(Features.info(paramClass()).singular(), el.getName());
    }
}
