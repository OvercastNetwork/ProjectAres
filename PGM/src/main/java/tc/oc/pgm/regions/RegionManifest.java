package tc.oc.pgm.regions;

import tc.oc.commons.core.inject.Manifest;
import tc.oc.pgm.features.FeatureBinder;
import tc.oc.pgm.map.MapRootParser;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.xml.parser.ParserBinders;

public class RegionManifest extends Manifest implements ParserBinders {
    @Override
    protected void configure() {
        bind(RegionDefinitionParser.class).in(MapScoped.class);
        bind(RegionParser.class).in(MapScoped.class);
        linkOptional(RegionParser.class);

        final FeatureBinder<Region> features = new FeatureBinder<>(binder(), Region.class);
        features.bindParser().to(RegionParser.class);
        features.bindDefinitionParser().to(RegionDefinitionParser.class);

        inSet(MapRootParser.class)
            .addBinding()
            .to(RegionParser.class)
            .in(MapScoped.class);
    }
}

