package tc.oc.pgm.xml.parser;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import tc.oc.commons.core.inject.Binders;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;

public interface ParserBinders extends Binders {

    default <T> LinkedBindingBuilder<Parser<T>> bindParser(Key<T> key) {
        return bind(key.ofType(Types.parameterizedTypeLiteral(Parser.class, key.getTypeLiteral())));
    }

    default <T> LinkedBindingBuilder<Parser<T>> bindParser(TypeLiteral<T> type) {
        return bindParser(Key.get(type));
    }

    default <T> LinkedBindingBuilder<Parser<T>> bindParser(Class<T> type) {
        return bindParser(Key.get(type));
    }

    default <T> LinkedBindingBuilder<PrimitiveParser<T>> bindPrimitiveParser(Key<T> key) {
        final Key<PrimitiveParser<T>> parserKey = key.ofType(PrimitiveParser.typeOf(key.getTypeLiteral()));
        bindParser(key).to(parserKey);
        return bind(parserKey);
    }

    default <T> LinkedBindingBuilder<PrimitiveParser<T>> bindPrimitiveParser(TypeLiteral<T> type) {
        return bindPrimitiveParser(Key.get(type));
    }

    default <T> LinkedBindingBuilder<PrimitiveParser<T>> bindPrimitiveParser(Class<T> type) {
        return bindPrimitiveParser(Key.get(type));
    }

    default <T> LinkedBindingBuilder<ElementParser<T>> bindElementParser(Key<T> key) {
        final Key<ElementParser<T>> parserKey = key.ofType(new ResolvableType<ElementParser<T>>(){}.with(new TypeArgument<T>(key.getTypeLiteral()){}));
        bindParser(key).to(parserKey);
        return bind(parserKey);
    }

    default <T> LinkedBindingBuilder<ElementParser<T>> bindElementParser(TypeLiteral<T> type) {
        return bindElementParser(Key.get(type));
    }

    default <T> LinkedBindingBuilder<ElementParser<T>> bindElementParser(Class<T> type) {
        return bindElementParser(Key.get(type));
    }
}
