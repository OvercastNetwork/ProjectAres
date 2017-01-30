package tc.oc.pgm.xml.parser;

import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.TypeManifest;

public class EnumParserManifest<T extends Enum<T>> extends TypeManifest<T> implements ParserBinders {

    private final TypeLiteral<EnumParser<T>> parserType;

    protected EnumParserManifest() {
        this((TypeLiteral) null);
    }

    public EnumParserManifest(Class<T> type) {
        this(TypeLiteral.get(type));
    }

    public EnumParserManifest(@Nullable TypeLiteral<T> type) {
        super(type);
        this.parserType = resolve(new TypeLiteral<EnumParser<T>>(){});
    }

    @Override
    protected void configure() {
        bind(parserType);
        bindPrimitiveParser(type).to(parserType);
    }
}
