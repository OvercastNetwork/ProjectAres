package tc.oc.commons.bukkit.settings;

import com.google.inject.Provides;
import me.anxuiz.settings.SettingCallbackManager;
import me.anxuiz.settings.SettingRegistry;
import me.anxuiz.settings.bukkit.PlayerSettings;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.inject.SingletonManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

/**
 * Configures the player settings system
 *
 * Provides the following service classes:
 *
 *   {@link SettingManagerProvider}     Access settings for specific players
 *   {@link SettingRegistry}            Register and lookup setting definitions
 *   {@link SettingCallbackManager}     Register setting change callbacks
 *
 * Also allows settings and callbacks to be registered at configuration time
 * using {@link SettingBinder} and {@link SettingCallbackBinder}.
 */
public class SettingManifest extends HybridManifest {
    public static class Public extends SingletonManifest {
        @Override
        protected void configure() {
            new SettingBinder(binder());
            new SettingCallbackBinder(binder());
        }

        @Provides
        SettingRegistry settingRegistry() {
            return PlayerSettings.getRegistry();
        }

        @Provides
        SettingCallbackManager settingCallbackManager() {
            return PlayerSettings.getCallbackManager();
        }
    }

    @Override
    protected void configure() {
        publicBinder().install(new Public());

        bind(SettingManagerProviderImpl.class);
        bindAndExpose(SettingManagerProvider.class).to(SettingManagerProviderImpl.class);
        new PluginFacetBinder(binder()).add(SettingManagerProviderImpl.class);
    }
}
