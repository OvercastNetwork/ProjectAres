package tc.oc.pgm.compose;

import javax.annotation.Nullable;

import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.pgm.xml.parser.ParserBinders;

public class ComposableManifest<T> extends KeyedManifest implements ParserBinders {

    private final TypeLiteral<T> type;
    private final TypeArgument<T> typeArg;

    protected ComposableManifest() {
        this(null);
    }

    public ComposableManifest(@Nullable TypeLiteral<T> type) {
        this.type = type != null ? type : new ResolvableType<T>(){}.in(getClass());
        this.typeArg = new TypeArgument<T>(this.type){};
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    @Override
    protected void configure() {
        bindElementParser(new ResolvableType<Composition<T>>(){}.with(typeArg))
            .to(new ResolvableType<CompositionParser<T>>(){}.with(typeArg));
    }
}
