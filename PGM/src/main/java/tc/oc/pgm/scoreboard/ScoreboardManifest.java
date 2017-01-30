package tc.oc.pgm.scoreboard;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.map.StaticMethodMapModuleFactory;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;

public class ScoreboardManifest extends HybridManifest {
    @Override
    protected void configure() {
        install(new MatchModuleFixtureManifest<ScoreboardMatchModule>(){});
        install(new StaticMethodMapModuleFactory<SidebarModule>(){});
        new SettingBinder(publicBinder()).addBinding().toInstance(ScoreboardSettings.SHOW_SCOREBOARD);
    }
}
