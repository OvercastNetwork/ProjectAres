package tc.oc.commons.bungee.inject;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import tc.oc.bungee.BungeeVersionInspector;
import tc.oc.commons.bungee.chat.BungeeAudiences;
import tc.oc.commons.bungee.logging.BungeeLoggerFactory;
import tc.oc.commons.bungee.plugin.BungeePluginResolver;
import tc.oc.commons.core.inject.SingletonManifest;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.server.MinecraftServerManifest;
import tc.oc.commons.core.plugin.PluginResolver;
import tc.oc.minecraft.VersionInspector;

public class BungeeServerManifest extends SingletonManifest {
    @Override
    protected void configure() {
        install(new MinecraftServerManifest());

        bind(new TypeLiteral<PluginResolver<Plugin>>(){}).to(BungeePluginResolver.class);
        bind(Loggers.class).to(BungeeLoggerFactory.class);
        bind(tc.oc.commons.core.chat.Audiences.class).to(tc.oc.commons.bungee.chat.Audiences.class);
        bind(tc.oc.commons.bungee.chat.Audiences.class).to(BungeeAudiences.class);
    }

    @Provides
    VersionInspector versionInspector(ProxyServer server) {
        return new BungeeVersionInspector(server);
    }
}
