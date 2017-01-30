package tc.oc.parse.primitive;

import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Types;
import tc.oc.parse.ParseException;
import tc.oc.parse.Parser;
import tc.oc.parse.ValueException;

public class EnumParser<T extends Enum<T>> implements Parser<T> {

    private final TypeLiteral<T> type;

    @Inject private EnumParser(TypeLiteral<T> type) {
        this.type = type;
    }

    @Override
    public TypeLiteral<T> paramLiteral() {
        return type;
    }

    @Override
    public TypeToken<T> paramToken() {
        return Types.toToken(type);
    }

    @Override
    public T parse(String text) throws ParseException {
        final Class<T> type = paramClass();
        text = text.trim().replace(' ', '_');
        try {
            // First, try the fast way
            return Enum.valueOf(type, text.toUpperCase());
        } catch(IllegalArgumentException ex) {
            // If that fails, search for a case-insensitive match, without assuming enums are always uppercase
            for(T value : type.getEnumConstants()) {
                if(value.name().equalsIgnoreCase(text)) return value;
            }
            throw new ValueException("Unknown " + readableTypeName() + " value '" + text + "'");
        }
    }
}
