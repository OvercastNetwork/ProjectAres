package tc.oc.pgm.wool;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.inject.MapScoped;

public class WoolManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(WoolParser.class).in(MapScoped.class);

        FeatureBinder<MonumentWoolFactory> wool = new FeatureBinder<>(binder(), MonumentWoolFactory.class);
        wool.bindDefinitionParser().to(WoolParser.class);
        wool.installRootParser();
        wool.installMatchModule(WoolMatchModule.class);
    }
}
