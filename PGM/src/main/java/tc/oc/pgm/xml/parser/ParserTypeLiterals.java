package tc.oc.pgm.xml.parser;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;

public interface ParserTypeLiterals {

    default <T> TypeLiteral<Parser<T>> Parser(TypeLiteral<T> type) {
        return new ResolvableType<Parser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<Parser<T>> Parser(Class<T> type) {
        return Parser(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<ElementParser<T>> ElementParser(TypeLiteral<T> type) {
        return new ResolvableType<ElementParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<ElementParser<T>> ElementParser(Class<T> type) {
        return ElementParser(TypeLiteral.get(type));
    }

    default <T extends Enum<T>> TypeLiteral<EnumParser<T>> EnumParser(TypeLiteral<T> type) {
        return new ResolvableType<EnumParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T extends Enum<T>> TypeLiteral<EnumParser<T>> EnumParser(Class<T> type) {
        return EnumParser(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<PrimitiveParser<T>> PrimitiveParser(TypeLiteral<T> type) {
        return new ResolvableType<PrimitiveParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<PrimitiveParser<T>> PrimitiveParser(Class<T> type) {
        return PrimitiveParser(TypeLiteral.get(type));
    }
}
