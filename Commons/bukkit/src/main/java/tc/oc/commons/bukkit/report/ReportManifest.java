package tc.oc.commons.bukkit.report;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class ReportManifest extends HybridManifest {

    @Override
    protected void configure() {
        new SettingBinder(publicBinder())
                .addBinding()
                .toInstance(ReportAnnouncer.SOUND_SETTING);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(ReportCommands.class);
        facets.register(ReportAnnouncer.class);
    }

}
