package tc.oc.commons.bukkit.users;

import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class JoinMessageManifest extends HybridManifest {
    @Override
    protected void configure() {
        new SettingBinder(publicBinder())
            .addBinding().toInstance(JoinMessageSetting.get());
        new PluginFacetBinder(binder())
            .add(JoinMessageAnnouncer.class);
    }
}
