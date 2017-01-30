package tc.oc.pgm.playerstats;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.MatchPlayerFacetBinder;
import tc.oc.pgm.match.MatchUserFacetBinder;
import tc.oc.pgm.match.inject.MatchBinders;

public class StatsManifest extends HybridManifest implements MatchBinders {

    @Override
    protected void configure() {
        new SettingBinder(publicBinder()).addBinding().toInstance(StatSettings.STATS);
        installPlayerModule(binder -> new MatchPlayerFacetBinder(binder).register(StatsPlayerFacet.class));
        installUserModule(binder -> new MatchUserFacetBinder(binder).register(StatsUserFacet.class));
    }

}
