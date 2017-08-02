package tc.oc.parse;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.parse.primitive.EnumParser;

/**
 * Configures parsing for enum type {@link T} using {@link EnumParser <T>}
 */
public class EnumParserManifest<T extends Enum<T>> extends KeyedManifest implements ParserTypeLiterals {

    private final TypeLiteral<T> T;

    public EnumParserManifest(Class<T> T) {
        this(TypeLiteral.get(T));
    }

    public EnumParserManifest(TypeLiteral<T> T) {
        this.T = T;
    }

    @Override
    protected Object manifestKey() {
        return T;
    }

    @Override
    protected void configure() {
        install(new PrimitiveParserManifest<>(T, EnumParser(T)));
    }
}
