package tc.oc.commons.bukkit.inject;

import java.io.File;
import java.nio.file.Path;
import javax.inject.Named;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.bukkit.bossbar.BossBarFactory;
import tc.oc.commons.bukkit.bossbar.BossBarFactoryImpl;
import tc.oc.commons.bukkit.chat.Audiences;
import tc.oc.commons.bukkit.item.RenderedItemBuilder;
import tc.oc.commons.bukkit.logging.BukkitLoggerFactory;
import tc.oc.commons.bukkit.permissions.BukkitPermissionRegistry;
import tc.oc.commons.bukkit.permissions.PermissionRegistry;
import tc.oc.commons.core.inject.SingletonManifest;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginResolver;
import tc.oc.commons.core.server.MinecraftServerManifest;

public class BukkitServerManifest extends SingletonManifest {

    @Override
    protected void configure() {
        install(new MinecraftServerManifest());

        final FactoryModuleBuilder fmb = new FactoryModuleBuilder();
        install(fmb.build(RenderedItemBuilder.Factory.class));

        bind(new TypeLiteral<PluginResolver<Plugin>>(){}).to(BukkitPluginResolver.class);
        bind(Loggers.class).to(BukkitLoggerFactory.class);
        bind(tc.oc.commons.core.chat.Audiences.class).to(Audiences.class);
        requestStaticInjection(Audiences.Deprecated.class);
        bind(PermissionRegistry.class).to(BukkitPermissionRegistry.class);
        bind(BossBarFactory.class).to(BossBarFactoryImpl.class);
    }

    @Provides @Named("serverRoot")
    File serverRootFile() {
        return new File(".").getAbsoluteFile();
    }

    @Provides @Named("serverRoot")
    Path serverRootPath() {
        return serverRootFile().toPath();
    }
}
