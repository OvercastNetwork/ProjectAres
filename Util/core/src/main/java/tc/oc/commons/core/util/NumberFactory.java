package tc.oc.commons.core.util;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import tc.oc.commons.core.reflect.Types;

public class NumberFactory<T extends Number> {

    private final Function<String, T> parser;
    private final T negativeInfinity, positiveInfinity;

    private NumberFactory(T negativeInfinity, T positiveInfinity, Function<String, T> parser) {
        this.negativeInfinity = negativeInfinity;
        this.positiveInfinity = positiveInfinity;
        this.parser = parser;
    }

    public T parse(String text) throws NumberFormatException {
        if("oo".equals(text)) {
            return infinity(true);
        } else if("-oo".equals(text)) {
            return infinity(false);
        } else {
            return parseFinite(text);
        }
    }

    public T parseFinite(String text) throws NumberFormatException {
        return parser.apply(text);
    }

    public T infinity(boolean sign) {
        return sign ? positiveInfinity : negativeInfinity;
    }

    public boolean isInfinite(T value) {
        return positiveInfinity.equals(value) || negativeInfinity.equals(value);
    }

    public boolean isFinite(T value) {
        return !isInfinite(value);
    }

    public static Set<Class<? extends Number>> numberTypes() {
        return byType.keySet();
    }

    public static <T extends Number> TypeToken<NumberFactory<T>> factoryType(Class<T> numberType) {
        return factoryType(TypeToken.of(numberType));
    }

    public static <T extends Number> TypeToken<NumberFactory<T>> factoryType(TypeToken<T> numberType) {
        return new TypeToken<NumberFactory<T>>(){}.where(new TypeParameter<T>(){}, numberType);
    }

    public static <T extends Number> NumberFactory<T> get(Class<T> type) {
        final NumberFactory<T> factory = (NumberFactory<T>) byType.get(type);
        if(factory == null) {
            throw new IllegalArgumentException("No NumberFactory for type " + type.getName());
        }
        return factory;
    }

    public static <T extends Number> Provider<NumberFactory<T>> provider(Class<T> type) {
        return () -> get(type);
    }

    private static final Map<Class<? extends Number>, NumberFactory<?>> byType = ImmutableMap
        .<Class<? extends Number>, NumberFactory<?>>builder()
        .put(Byte.class, new NumberFactory<>(Byte.MIN_VALUE, Byte.MAX_VALUE, Byte::valueOf))
        .put(Short.class, new NumberFactory<>(Short.MIN_VALUE, Short.MAX_VALUE, Short::valueOf))
        .put(Integer.class, new NumberFactory<>(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::valueOf))
        .put(Long.class, new NumberFactory<>(Long.MIN_VALUE, Long.MAX_VALUE, Long::valueOf))
        .put(Float.class, new NumberFactory<>(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float::valueOf))
        .put(Double.class, new NumberFactory<>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double::valueOf))
        .build();

    public static class Manifest extends AbstractModule {
        @Override
        protected void configure() {
            numberTypes().forEach(this::configure);
        }

        <T extends Number> void configure(Class<T> type) {
            bind(Types.toLiteral(NumberFactory.factoryType(type))).toProvider(NumberFactory.provider(type));
        }
    }
}
