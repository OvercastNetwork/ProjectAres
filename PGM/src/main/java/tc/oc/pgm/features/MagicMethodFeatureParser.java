package tc.oc.pgm.features;

import org.jdom2.Element;
import tc.oc.pgm.utils.MethodParser;
import tc.oc.pgm.utils.MethodParserMap;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.UnrecognizedXMLException;

/**
 * A feature definition parser that uses {@link MethodParserMap}
 */
public abstract class MagicMethodFeatureParser<T extends FeatureDefinition> implements FeatureDefinitionParser<T> {

    private final String featureName;
    private final MethodParserMap<T> methodParsers;

    protected MagicMethodFeatureParser() {
        featureName = Features.name(paramClass());
        methodParsers = new MethodParserMap<>(paramToken());
        methodParsers.register(this);
    }

    /**
     * Parse a {@link T} defined by the given {@link Element}.
     *
     * @throws InvalidXMLException if no {@link MethodParser} exists for the given element
     */
    @Override
    public T parseElement(Element el) throws InvalidXMLException {
        if(methodParsers.canParse(el)) {
            return methodParsers.parse(el);
        }
        throw new UnrecognizedXMLException(featureName, el);
    }

    /**
     * Can the given {@link Element} be parsed as a definition? (not a reference)
     */
    @Override
    public boolean isDefinition(Element el) throws InvalidXMLException {
        return methodParsers.canParse(el);
    }
}
