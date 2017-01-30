package tc.oc.pgm.features;

import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import org.jdom2.Element;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ReflectiveParser;

/**
 * Reflectively parses definitions of feature {@link T} using its {@link ReflectiveParser}.
 */
public class ReflectiveFeatureParser<T extends FeatureDefinition> implements FeatureDefinitionParser<T> {

    private final TypeToken<T> type;
    private final ReflectiveParser<T> reflectiveParser;

    @Inject private ReflectiveFeatureParser(TypeLiteral<T> type, ReflectiveParser<T> reflectiveParser) {
        this.type = Types.toToken(type);
        this.reflectiveParser = reflectiveParser;
    }

    @Override
    public TypeToken<T> paramToken() {
        return type;
    }

    @Override
    public boolean isDefinition(Element el) throws InvalidXMLException {
        return true; // Assume any element that is not a reference is a definition
    }

    @Override
    public T parseElement(Element el) throws InvalidXMLException {
        return reflectiveParser.parseElement(el);
    }
}
