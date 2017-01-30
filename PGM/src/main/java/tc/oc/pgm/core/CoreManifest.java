package tc.oc.pgm.core;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapScoped;

public class CoreManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(CoreParser.class).in(MapScoped.class);

        final FeatureBinder<CoreFactory> core = new FeatureBinder<>(binder(), CoreFactory.class);
        core.bindDefinitionParser().to(CoreParser.class);
        core.installRootParser();
        core.installMatchModule(CoreMatchModule.class);
    }
}
