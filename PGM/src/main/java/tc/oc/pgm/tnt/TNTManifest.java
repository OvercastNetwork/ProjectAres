package tc.oc.pgm.tnt;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.MatchUserFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;
import tc.oc.pgm.tnt.license.LicenseAccessPlayerFacet;
import tc.oc.pgm.tnt.license.LicenseMonitorUserFacet;

public class TNTManifest extends HybridManifest implements MapBinders, MatchBinders {

    @Override
    protected void configure() {
        bindRootElementParser(TNTProperties.class)
            .to(TNTParser.class);

        install(new MatchModuleFixtureManifest<TNTMatchModule>(){});

        installUserModule(binder -> {
            new MatchUserFacetBinder(binder).register(LicenseMonitorUserFacet.class);
        });

        installPlayerModule(binder -> {
            new MatchPlayerFacetBinder(binder).register(LicenseAccessPlayerFacet.class);
        });
    }
}
