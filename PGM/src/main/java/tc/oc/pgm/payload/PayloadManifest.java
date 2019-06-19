package tc.oc.pgm.payload;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.inject.MatchBinders;

public class PayloadManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bind(PayloadParser.class).in(MapScoped.class);

        final FeatureBinder<PayloadDefinition> cp = new FeatureBinder<>(binder(), PayloadDefinition.class);
        cp.bindDefinitionParser().to(PayloadParser.class);
        cp.installMatchModule(PayloadMatchModule.class);
        cp.installRootParser(new PayloadRootNodeFinder());
    }
}
