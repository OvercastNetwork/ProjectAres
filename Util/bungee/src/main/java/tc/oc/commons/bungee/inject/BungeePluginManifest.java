package tc.oc.commons.bungee.inject;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import tc.oc.bungee.configuration.YamlConfigurationLoader;
import tc.oc.commons.bungee.commands.BungeeCommandRegistry;
import tc.oc.commons.bungee.scheduler.BungeeSchedulerManifest;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.commands.CommandRegistryImpl;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.MinecraftPluginManifest;
import tc.oc.commons.core.plugin.PluginFacetManifest;
import tc.oc.commons.core.plugin.PluginScoped;

public class BungeePluginManifest extends HybridManifest {

    @Override
    protected void configure() {
        publicBinder().install(new BungeeServerManifest());
        install(new MinecraftPluginManifest());
        install(new BungeeSchedulerManifest());
        install(new PluginFacetManifest());

        bind(CommandRegistry.class).to(BungeeCommandRegistry.class);
        bind(new TypeLiteral<CommandRegistryImpl<CommandSender>>(){}).to(BungeeCommandRegistry.class);

        bind(tc.oc.minecraft.api.configuration.Configuration.class).to(net.md_5.bungee.config.Configuration.class);
    }

    @Provides @PluginScoped
    net.md_5.bungee.config.Configuration configuration(Plugin plugin) {
        return new YamlConfigurationLoader(plugin).loadConfig();
    }
}
