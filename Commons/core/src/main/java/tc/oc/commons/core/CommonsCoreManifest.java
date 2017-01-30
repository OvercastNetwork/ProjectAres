package tc.oc.commons.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Provides;
import tc.oc.analytics.AnalyticsManifest;
import tc.oc.analytics.datadog.DataDogManifest;
import tc.oc.commons.core.commands.DebugCommands;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.Manifest;
import tc.oc.commons.core.localization.LocalizedFileManager;
import tc.oc.commons.core.plugin.PluginFacetBinder;
import tc.oc.commons.core.restart.RestartManager;
import tc.oc.file.PathWatcherService;
import tc.oc.file.PathWatcherServiceImpl;
import tc.oc.minecraft.analytics.MinecraftAnalyticsManifest;
import tc.oc.minecraft.server.ServerFilterManifest;
import tc.oc.minecraft.suspend.SuspendableBinder;

public class CommonsCoreManifest extends HybridManifest {
    @Override
    protected void configure() {
        publicBinder().install(new PathsManifest());
        publicBinder().install(new ServerFilterManifest());

        new SuspendableBinder(publicBinder()); // Just bind the Set

        install(new AnalyticsManifest());
        install(new MinecraftAnalyticsManifest());
        install(new DataDogManifest());

        bindAndExpose(RestartManager.class);
        bindAndExpose(LocalizedFileManager.class);

        expose(PathWatcherService.class);
        bind(PathWatcherService.class)
            .to(PathWatcherServiceImpl.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(DebugCommands.class);
        facets.register(RestartManager.class);
        facets.register(PathWatcherServiceImpl.class);
    }

    class PathsManifest extends Manifest {
        @Provides @Singleton
        @Named("repositories") Path repositoriesPath() {
            return Paths.get("/minecraft/repo");
        }

        @Provides @Singleton
        @Named("translations") Path translationsPath() {
            return Paths.get("/minecraft/translations");
        }

        @Provides @Singleton
        @Named("maps") Path mapsPath(@Named("repositories") Path repos) {
            return repos.resolve("maps");
        }

        @Provides @Singleton
        @Named("configuration") Path configurationPath(@Named("repositories") Path repos) {
            return repos.resolve("Config");
        }
    }
}
