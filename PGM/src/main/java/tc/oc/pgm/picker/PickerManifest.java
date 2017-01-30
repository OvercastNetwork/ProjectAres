package tc.oc.pgm.picker;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.pgm.match.inject.MatchModuleFixtureManifest;

public class PickerManifest extends HybridManifest {
    @Override
    protected void configure() {
        new SettingBinder(publicBinder()).addBinding().toInstance(PickerSettings.PICKER);
        install(new MatchModuleFixtureManifest<PickerMatchModule>(){});
    }
}
