package tc.oc.pgm.match;

import com.google.inject.AbstractModule;
import tc.oc.pgm.cooldown.CooldownPlayerFacet;
import tc.oc.pgm.kits.AttributePlayerFacet;
import tc.oc.pgm.map.MapmakerPlayerFacet;
import tc.oc.pgm.portals.PortalPlayerFacet;
import tc.oc.pgm.projectile.ProjectilePlayerFacet;

/**
 * Binds all {@link MatchPlayerFacet}s.
 */
public class MatchPlayerFacetManifest extends AbstractModule {

    @Override
    protected void configure() {
        final MatchPlayerFacetBinder facets = new MatchPlayerFacetBinder(binder());

        // Fixture facets, always loaded
        facets.register(CooldownPlayerFacet.class);
        facets.register(MapmakerPlayerFacet.class);
        facets.register(AttributePlayerFacet.class);
        facets.register(MatchPlayerExecutor.class);
        facets.register(PortalPlayerFacet.class);
        facets.register(ProjectilePlayerFacet.class);
    }
}
