package tc.oc.pgm.analytics;

import tc.oc.analytics.TaggerBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.pgm.match.inject.MatchScoped;

public class MatchAnalyticsManifest extends HybridManifest {
    @Override
    protected void configure() {
        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(MatchPlayerReporter.class);
        facets.register(PlayerVersionLogger.class);

        bind(MatchTagger.class).in(MatchScoped.class);
        expose(MatchTagger.class);

        new TaggerBinder(publicBinder())
            .addBinding().to(MatchTagger.class);
    }
}
