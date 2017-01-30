package tc.oc.pgm.features;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.jdom2.Document;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.inject.KeyedManifest;
import tc.oc.commons.core.reflect.ResolvableType;
import tc.oc.commons.core.reflect.TypeArgument;
import tc.oc.commons.core.reflect.Types;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.module.ModuleExceptionHandler;
import tc.oc.pgm.xml.ElementFlattener;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.finder.NodeFinder;
import tc.oc.pgm.xml.parser.Parser;

/**
 * Configures a {@link FeatureDefinition} that is parsed from the root
 * of the {@link Document} using a {@link NodeFinder}, and binds the
 * results into List<T> in {@link MapScoped}.
 *
 * If no {@link NodeFinder} is specified, a typical structure is assumed,
 * based on the {@link FeatureInfo} annotation present on {@link T}.
 */
public class RootFeatureManifest<T extends FeatureDefinition> extends KeyedManifest implements MapBinders {

    private final TypeLiteral<T> featureType;
    private final TypeArgument<T> featureTypeArg;
    private final TypeLiteral<List<T>> featureListType;
    private final Class<T> rawType;
    private final NodeFinder nodeFinder;

    protected RootFeatureManifest() {
        this(null);
    }

    public RootFeatureManifest(@Nullable TypeLiteral<T> type) {
        this(type, null);
    }

    public RootFeatureManifest(@Nullable Class<T> type, @Nullable NodeFinder nodeFinder) {
        this(type == null ? null : TypeLiteral.get(type), nodeFinder);
    }

    public RootFeatureManifest(@Nullable TypeLiteral<T> type, @Nullable NodeFinder nodeFinder) {
        this.featureType = type != null ? type : new ResolvableType<T>(){}.in(getClass());
        this.featureTypeArg = new TypeArgument<T>(this.featureType){};
        this.featureListType = new ResolvableType<List<T>>(){}.with(featureTypeArg);
        this.rawType = (Class<T>) featureType.getRawType();

        if(nodeFinder == null) {
            final FeatureInfo info = Features.info(rawType);

            final Set<String> singular = info.singular().length > 0 ? ImmutableSet.copyOf(info.singular())
                                                                    : ImmutableSet.of(info.name());

            final Set<String> plural = info.plural().length > 0 ? ImmutableSet.copyOf(info.plural())
                                                                : singular.stream()
                                                                          .map(StringUtils::pluralize)
                                                                          .collect(Collectors.toImmutableSet());

            final ElementFlattener flattener = new ElementFlattener(plural, singular, 1);
            nodeFinder = (parent, name) -> flattener.flattenChildren(parent).map(Node::of);
        }
        this.nodeFinder = nodeFinder;
    }

    @Override
    protected Object manifestKey() {
        return featureType;
    }

    @Override
    protected void configure() {
        provisionAtParseTime(featureListType)
            .toProvider(new FeatureListProvider())
            .in(MapScoped.class);
    }

    private class FeatureListProvider implements Provider<List<T>> {

        @Inject Provider<Document> documentProvider;
        @Inject Provider<ModuleExceptionHandler> exceptionHandlerProvider;

        final Provider<FeatureParser<T>> parserProvider = getProvider(Key.get(Types.parameterizedTypeLiteral(FeatureParser.class, featureType)));

        @Override
        public List<T> get() {
            final Document document = documentProvider.get();
            final ModuleExceptionHandler exceptionHandler = exceptionHandlerProvider.get();
            final Parser<T> parser = parserProvider.get();

            // Skip over features that throw and keep loading the rest.
            return nodeFinder.findNodes(document.getRootElement(), "")
                             .flatMap(child -> Optionals.stream(exceptionHandler.ignoringFailures(() -> parser.parse(child))))
                             .collect(Collectors.toImmutableList());
        }
    }
}
