package tc.oc.pgm.physics;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Keys;
import tc.oc.pgm.map.inject.MapBinders;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class PlayerPhysicsManifest extends HybridManifest implements MapBinders, MatchBinders {
    @Override
    protected void configure() {
        bindRootElementParser(Keys.optional(KnockbackSettings.class))
            .to(KnockbackParser.class);

        installPlayerModule(binder -> {
            final MatchPlayerFacetBinder facets = new MatchPlayerFacetBinder(binder);
            facets.register(AccelerationPlayerFacet.class);
            facets.register(DebugVelocityPlayerFacet.class);
            facets.register(KnockbackPlayerFacet.class);
        });
    }
}
