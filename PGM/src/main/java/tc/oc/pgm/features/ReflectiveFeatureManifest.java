package tc.oc.pgm.features;

import javax.annotation.Nullable;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.xml.parser.ReflectiveParserManifest;

/**
 * Configures a reflectively parsed feature {@link T}, which must be an interface.
 *
 * @see tc.oc.pgm.xml.Parseable for details on reflective parsing
 */
public class ReflectiveFeatureManifest<T extends FeatureDefinition> extends KeyedManifest implements MapBinders {

    private final TypeLiteral<T> type;
    private final TypeArgument<T> typeArg;
    private final Key<FeatureDefinitionParser<T>> definitionParserKey;

    protected ReflectiveFeatureManifest() {
        this(null);
    }

    public ReflectiveFeatureManifest(@Nullable TypeLiteral<T> type) {
        this.type = type != null ? Types.assertFullySpecified(type)
                                 : new ResolvableType<T>(){}.in(getClass());
        this.typeArg = new TypeArgument<T>(this.type){};
        this.definitionParserKey = Key.get(new ResolvableType<FeatureDefinitionParser<T>>(){}.with(typeArg));
    }

    @Override
    protected Object manifestKey() {
        return type;
    }

    @Override
    protected void configure() {
        // Generate the reflective parser and bind it to ReflectiveParser<T>
        install(new ReflectiveParserManifest<>(type, FeatureDefinition.Impl.class));

        // Bind ReflectiveFeatureParser<T> as the definition parser for T
        bind(definitionParserKey).to(new ResolvableType<ReflectiveFeatureParser<T>>(){}.with(typeArg))
                                 .in(MapScoped.class);
    }
}
