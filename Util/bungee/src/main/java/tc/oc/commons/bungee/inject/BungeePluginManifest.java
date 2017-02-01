package tc.oc.commons.bungee.inject;

import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.CommandSender;
import tc.oc.commons.bungee.commands.BungeeCommandRegistry;
import tc.oc.commons.bungee.scheduler.BungeeSchedulerManifest;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.commands.CommandRegistryImpl;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.MinecraftPluginManifest;
import tc.oc.commons.core.plugin.PluginFacetManifest;

public class BungeePluginManifest extends HybridManifest {

    @Override
    protected void configure() {
        publicBinder().install(new BungeeServerManifest());
        install(new MinecraftPluginManifest());
        install(new BungeeSchedulerManifest());
        install(new PluginFacetManifest());

        bind(CommandRegistry.class).to(BungeeCommandRegistry.class);
        bind(new TypeLiteral<CommandRegistryImpl<CommandSender>>(){}).to(BungeeCommandRegistry.class);
    }
}
