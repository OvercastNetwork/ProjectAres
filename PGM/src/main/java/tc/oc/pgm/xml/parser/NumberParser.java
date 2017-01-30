package tc.oc.pgm.xml.parser;

import javax.inject.Inject;

import com.google.common.cache.LoadingCache;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.NumberFactory;
import tc.oc.pgm.xml.Node;

public class NumberParser<T extends Number> extends TransfiniteParser<T> {

    private final NumberFactory<T> factory;

    @Inject private NumberParser(TypeLiteral<T> type, NumberFactory<T> factory) {
        super(Types.toToken(type));
        this.factory = factory;
    }

    private NumberParser(Class<T> type) {
        this(TypeLiteral.get(type), NumberFactory.get(type));
    }

    private static final LoadingCache<Class<? extends Number>, NumberParser<?>> byType = CacheUtils.newCache(NumberParser::new);

    @Deprecated // @Inject me!
    public static <T extends Number> NumberParser<T> get(Class<T> type) {
        return (NumberParser<T>) byType.getUnchecked(type);
    }

    @Override
    protected T infinity(boolean sign) {
        return factory.infinity(sign);
    }

    @Override
    protected T parseFinite(Node node, String text) throws FormatException {
        try {
            return factory.parseFinite(text);
        } catch(NumberFormatException e) {
            throw new FormatException();
        }
    }
}
