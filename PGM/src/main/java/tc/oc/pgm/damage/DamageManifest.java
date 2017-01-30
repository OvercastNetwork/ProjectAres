package tc.oc.pgm.damage;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.StaticMethodMapModuleFactory;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class DamageManifest extends HybridManifest implements MatchBinders {
    @Override
    protected void configure() {
        install(new DamageModule.Factory());
        install(new StaticMethodMapModuleFactory<DisableDamageModule>(){});

        final SettingBinder settings = new SettingBinder(publicBinder());
        settings.addBinding().toInstance(DamageSettings.ATTACK_SPEEDOMETER);
        settings.addBinding().toInstance(DamageSettings.DAMAGE_NUMBERS);
        settings.addBinding().toInstance(DamageSettings.KNOCKBACK_PARTICLES);

        installPlayerModule(binder -> {
            final MatchPlayerFacetBinder facets = new MatchPlayerFacetBinder(binder);
            facets.register(DamageDisplayPlayerFacet.class);
            facets.register(HitboxPlayerFacet.class);
        });
    }
}
