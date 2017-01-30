package tc.oc.pgm.features;

import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.xml.parser.ElementParser;
import tc.oc.pgm.xml.parser.Parser;
import tc.oc.pgm.xml.parser.ParserTypeLiterals;
import tc.oc.pgm.xml.parser.PrimitiveParser;

/**
 * Links {@link Parser}, {@link ElementParser}, and {@link PrimitiveParser} to {@link FeatureParser} for feature {@link T}.
 *
 * This manifest does not bind {@link FeatureParser} explicitly, but it is eligible for JIT binding.
 */
public class FeatureManifest<T extends FeatureDefinition> extends KeyedManifest implements ParserTypeLiterals, FeatureTypeLiterals {

    private final TypeLiteral<T> T;

    protected FeatureManifest() {
        this((TypeLiteral<T>) null);
    }

    public FeatureManifest(@Nullable Class<T> T) {
        this(TypeLiteral.get(T));
    }

    public FeatureManifest(@Nullable TypeLiteral<T> T) {
        this.T = T != null ? Types.assertFullySpecified(T)
                           : new ResolvableType<T>(){}.in(getClass());
    }

    @Override
    protected Object manifestKey() {
        return T;
    }

    @Override
    protected void configure() {
        bind(Parser(T)).to(ElementParser(T));
        bind(ElementParser(T)).to(FeatureParser(T));
        bind(PrimitiveParser(T)).to(FeatureParser(T));
    }
}
