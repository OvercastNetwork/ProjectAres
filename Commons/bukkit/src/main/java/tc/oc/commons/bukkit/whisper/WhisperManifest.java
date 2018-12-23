package tc.oc.commons.bukkit.whisper;

import javax.inject.Singleton;
import tc.oc.commons.bukkit.settings.SettingBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class WhisperManifest extends HybridManifest {
    @Override
    protected void configure() {
        final SettingBinder settings = new SettingBinder(publicBinder());
        settings.addBinding().toInstance(WhisperSettings.receive());
        settings.addBinding().toInstance(WhisperSettings.sound());

        bind(WhisperFormatter.class);
        bind(WhisperCommands.class).in(Singleton.class);
        bind(WhisperDispatcher.class).in(Singleton.class);
        bind(WhisperSender.class).to(WhisperDispatcher.class);
        expose(WhisperSender.class);

        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.add(WhisperDispatcher.class);
        facets.add(WhisperCommands.class);
    }
}
