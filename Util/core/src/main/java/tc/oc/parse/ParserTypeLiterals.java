package tc.oc.parse;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.parse.primitive.EnumParser;
import tc.oc.parse.xml.DocumentParser;
import tc.oc.parse.xml.ElementParser;
import tc.oc.parse.xml.NodeParser;
import tc.oc.parse.xml.PrimitiveNodeParser;

public interface ParserTypeLiterals {

    default <T> TypeLiteral<Parser<T>> Parser(TypeLiteral<T> type) {
        return new ResolvableType<Parser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<Parser<T>> Parser(Class<T> type) {
        return Parser(TypeLiteral.get(type));
    }

    default <T extends Enum<T>> TypeLiteral<EnumParser<T>> EnumParser(TypeLiteral<T> type) {
        return new ResolvableType<EnumParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T extends Enum<T>> TypeLiteral<EnumParser<T>> EnumParser(Class<T> type) {
        return EnumParser(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<NodeParser<T>> NodeParser(TypeLiteral<T> type) {
        return new ResolvableType<NodeParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<NodeParser<T>> NodeParser(Class<T> type) {
        return NodeParser(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<PrimitiveNodeParser<T>> PrimitiveNodeParser(TypeLiteral<T> type) {
        return new ResolvableType<PrimitiveNodeParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<PrimitiveNodeParser<T>> PrimitiveNodeParser(Class<T> type) {
        return PrimitiveNodeParser(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<ElementParser<T>> ElementParser(TypeLiteral<T> type) {
        return new ResolvableType<ElementParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<ElementParser<T>> ElementParser(Class<T> type) {
        return ElementParser(TypeLiteral.get(type));
    }

    default <T> TypeLiteral<DocumentParser<T>> DocumentParser(TypeLiteral<T> type) {
        return new ResolvableType<DocumentParser<T>>(){}.with(new TypeArgument<T>(type){});
    }

    default <T> TypeLiteral<DocumentParser<T>> DocumentParser(Class<T> type) {
        return DocumentParser(TypeLiteral.get(type));
    }
}
