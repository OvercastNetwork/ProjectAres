package tc.oc.pgm.xml.parser;

import javax.annotation.Nullable;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

/**
 * Parser for a type {@link T} that can be represented as a single {@link String}
 *
 * The source {@link Node} and {@link String} can be specified seperately,
 * allowing multiple instances to be parsed from a single {@link Node}.
 */
public abstract class PrimitiveParser<T> implements Parser<T> {

    protected final @Nullable TypeToken<T> type;

    protected PrimitiveParser(@Nullable TypeToken<T> type) {
        this.type = type;
    }

    protected PrimitiveParser() {
        this(null);
    }

    @Override
    public TypeToken<T> paramToken() {
        return type != null ? type : Parser.super.paramToken();
    }

    @Override
    public T parseInternal(Node node) throws InvalidXMLException {
        try {
            return parseInternal(node, node.getValue());
        } catch(FormatException e) {
            throw wrapFormatException(node, e);
        }
    }

    protected abstract T parseInternal(Node node, String text) throws FormatException, InvalidXMLException;

    public T parse(Node node, String text) throws InvalidXMLException {
        return InvalidXMLException.offeringNode(node, () -> {
            try {
                return parseInternal(node, text);
            } catch(FormatException e) {
                throw wrapFormatException(node, e);
            }
        });
    }

    protected InvalidXMLException wrapFormatException(Node node, FormatException e) {
        String message = "Invalid " + readableTypeName() + " format";
        if(e.getMessage() != null) {
            message += ": " + e.getMessage();
        }
        return new InvalidXMLException(message, node);
    }

    public static class FormatException extends Exception {
        public FormatException() {}

        public FormatException(@Nullable String message) {
            super(message);
        }
    }

    static <T> TypeToken<PrimitiveParser<T>> typeOf(TypeToken<T> type) {
        return new TypeToken<PrimitiveParser<T>>(){}.where(new TypeParameter<T>(){}, type);
    }

    static <T> TypeLiteral<PrimitiveParser<T>> typeOf(TypeLiteral<T> type) {
        return Types.toLiteral(typeOf(Types.toToken(type)));
    }
}
