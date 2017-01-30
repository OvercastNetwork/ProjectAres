package tc.oc.pgm.features;

import javax.annotation.Nullable;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.inject.MatchModuleFeatureManifest;
import tc.oc.pgm.xml.finder.NodeFinder;

/**
 * Shortcuts for configuring feature {@link T}
 */
public class FeatureBinder<T extends FeatureDefinition> {
    
    private final Binder binder;
    private final TypeLiteral<T> type;
    private final TypeArgument<T> typeArg;
    private final Key<T> key;

    protected FeatureBinder(Binder binder) {
        this(binder, (TypeLiteral<T>) null);
    }

    public FeatureBinder(Binder binder, Class<T> type) {
        this(binder, TypeLiteral.get(type));
    }

    public FeatureBinder(Binder binder, @Nullable TypeLiteral<T> type) {
        this.binder = binder;
        this.type = type != null ? type : new ResolvableType<T>(){}.in(getClass());
        this.typeArg = new TypeArgument<T>(this.type){};
        this.key = Key.get(this.type);

        binder.install(new FeatureManifest<>(this.type));
    }

    /**
     * Bind a specialized {@link FeatureParser} for {@link T}.
     *
     * Without this, {@link FeatureParser} itself will be used to parse {@link T}.
     */
    public LinkedBindingBuilder<FeatureParser<T>> bindParser() {
        return binder.bind(key.ofType(new ResolvableType<FeatureParser<T>>(){}.with(typeArg)));
    }

    /**
     * Bind the {@link FeatureDefinitionParser} for {@link T}.
     *
     * This binding must exist for {@link T} to be parseable.
     */
    public LinkedBindingBuilder<FeatureDefinitionParser<T>> bindDefinitionParser() {
        return binder.bind(key.ofType(new ResolvableType<FeatureDefinitionParser<T>>(){}.with(typeArg)));
    }

    /**
     * Configure a List of {@link T} to be parsed from the root of the document automatically,
     * using the default element structure.
     *
     * @see RootFeatureManifest for details
     */
    public void installRootParser() {
        installRootParser(null);
    }

    /**
     * Configure a List of {@link T} to be parsed from the root of the document automatically,
     * using the given {@link NodeFinder}.
     *
     * @see RootFeatureManifest for details
     */
    public void installRootParser(@Nullable NodeFinder nodeFinder) {
        binder.install(new RootFeatureManifest<>(type, nodeFinder));
    }

    /**
     * Generate a reflective parser for {@link T}.
     *
     * @see ReflectiveFeatureManifest
     * @see tc.oc.pgm.xml.Parseable
     */
    public void installReflectiveParser() {
        binder.install(new ReflectiveFeatureManifest<>(type));
    }

    /**
     * Configure the given {@link MatchModule} to load if any instances of {@link T} were parsed
     *
     * @see MatchModuleFeatureManifest
     */
    public void installMatchModule(Class<? extends MatchModule> matchModule) {
        binder.install(new MatchModuleFeatureManifest<>(TypeLiteral.get(matchModule), type));
    }
}
