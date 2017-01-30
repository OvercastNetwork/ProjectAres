package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class EnumParser<T extends Enum<T>> extends PrimitiveParser<T> {

    @Inject private EnumParser(TypeLiteral<T> type) {
        super(Types.toToken(type));
    }

    @Override
    public T parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        final Class<T> type = paramClass();
        text = text.trim().replace(' ', '_');
        try {
            // First, try the fast way
            return Enum.valueOf(type, text);
        } catch(IllegalArgumentException ex) {
            // If that fails, search for a case-insensitive match, without assuming enums are always uppercase
            for(T value : type.getEnumConstants()) {
                if(value.name().equalsIgnoreCase(text)) return value;
            }
            throw new InvalidXMLException("Unknown " + readableTypeName() + " value '" + text + "'", node);
        }
    }
}
