package tc.oc.pgm.filters;

import javax.inject.Provider;

import com.google.inject.Provides;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.filters.matcher.CauseFilter;
import tc.oc.pgm.filters.parser.FilterDefinitionParser;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.filters.parser.LegacyFilterDefinitionParser;
import tc.oc.pgm.filters.parser.LegacyFilterParser;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchScoped;
import tc.oc.pgm.xml.parser.ParserBinders;

public class FilterManifest extends HybridManifest implements MatchBinders, ParserBinders {

    @Override
    protected void configure() {
        installFactory(CauseFilter.Factory.class);

        final FeatureBinder<Filter> features = new FeatureBinder<>(binder(), Filter.class);
        features.bindParser().to(FilterParser.class);

        bind(LegacyFilterParser.class).in(MapScoped.class);
        bind(ModernFilterParser.class).in(MapScoped.class);

        bind(LegacyFilterDefinitionParser.class).in(MapScoped.class);
        bind(FilterDefinitionParser.class).in(MapScoped.class);

        bind(FilterMatchModule.class).in(MatchScoped.class);
        bind(FilterDispatcher.class).to(FilterMatchModule.class);

        linkOptional(FilterMatchModule.class);
        linkOptional(FilterParser.class);

        matchListener(FilterMatchModule.class);

        inSet(MapRootParser.class)
            .addBinding()
            .to(FilterParser.class)
            .in(MapScoped.class);
    }

    @Provides @MapScoped
    protected FilterParser filterParser(@MapProto SemanticVersion proto, Provider<ModernFilterParser> modern, Provider<LegacyFilterParser> legacy) {
        return (proto.isOlderThan(ProtoVersions.FILTER_FEATURES) ? legacy : modern).get();
    }

    @Provides @MapScoped
    FeatureDefinitionParser<Filter> filterDefinitionParser(@MapProto SemanticVersion proto, Provider<FilterDefinitionParser> modern, Provider<LegacyFilterDefinitionParser> legacy) {
        return (proto.isOlderThan(ProtoVersions.FILTER_FEATURES) ? legacy : modern).get();
    }
}

// This allows us to bind FilterParser to the provider above
class ModernFilterParser extends FilterParser {}