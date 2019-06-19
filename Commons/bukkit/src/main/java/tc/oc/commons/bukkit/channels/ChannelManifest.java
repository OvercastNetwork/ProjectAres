package tc.oc.commons.bukkit.channels;

import tc.oc.commons.bukkit.channels.admin.AdminChannel;
import tc.oc.commons.bukkit.channels.server.ServerChannel;
import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class ChannelManifest extends HybridManifest {
    @Override
    protected void configure() {

        bindAndExpose(ChannelRouter.class);
        expose(ChannelCommands.class);
        expose(AdminChannel.class);
        expose(ServerChannel.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(ChannelCommands.class);
        facets.register(AdminChannel.class);
        facets.register(ServerChannel.class);

        final SettingBinder settings = new SettingBinder(publicBinder());
        settings.addBinding().toInstance(AdminChannel.SETTING);
    }
}
