package tc.oc.analytics;

import tc.oc.commons.core.inject.HybridManifest;

public class AnalyticsManifest extends HybridManifest {

    @Override
    protected void configure() {
        installFactory(MetricFactory.class);
        bind(DynamicTagger.class);

        expose(MetricFactory.class);
        expose(DynamicTagger.class);
        expose(AnalyticsClient.class);

        final TaggerBinder taggers = new TaggerBinder(publicBinder());
        taggers.addBinding().to(StageTagger.class);
    }
}
