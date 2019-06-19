package tc.oc.commons.bukkit.settings;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingCallback;
import me.anxuiz.settings.SettingCallbackManager;
import me.anxuiz.settings.SettingManager;
import me.anxuiz.settings.SettingRegistry;
import me.anxuiz.settings.TypeParseException;
import me.anxuiz.settings.base.AbstractSettingManager;
import me.anxuiz.settings.bukkit.PlayerSettingCallback;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.bukkit.users.OnlinePlayers;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.User;
import tc.oc.api.users.ChangeSettingRequest;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.event.UserLoginEvent;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * A singleton with several responsibilities related to player settings:
 *
 * - Provides access to settings through the {@link SettingManagerProvider} interface
 * - Loads saved settings on player login
 * - Listens for setting changes and saves them
 * - Registers settings and callbacks that were bound at configuration time
 */
@Singleton
class SettingManagerProviderImpl implements SettingManagerProvider, Listener, PluginFacet {

    private final Server localServer;
    private final BukkitUserStore userStore;
    private final UserService userService;
    private final OnlinePlayers onlinePlayers;
    private final SettingRegistry settingRegistry;

    @Inject
    SettingManagerProviderImpl(Server localServer,
                               SettingCallbackManager callbackManager,
                               BukkitUserStore userStore,
                               UserService userService,
                               OnlinePlayers onlinePlayers,
                               SettingRegistry settingRegistry,
                               Set<Setting> settings,
                               Map<Setting, SettingCallback> callbacks) {

        this.localServer = localServer;
        this.userStore = userStore;
        this.userService = userService;
        this.onlinePlayers = onlinePlayers;
        this.settingRegistry = settingRegistry;

        callbackManager.addGlobalCallback(new Callback());

        settings.forEach(settingRegistry::register);
        callbacks.forEach(callbackManager::addCallback);
    }

    @Override
    public SettingManager getManager(Player player) {
        return me.anxuiz.settings.bukkit.PlayerSettings.getManager(player);
    }

    @Override
    public SettingManager getManager(User user) {
        final Player player = onlinePlayers.find(user);
        return player != null ? getManager(player)
                              : new UserSettingManager(user);
    }

    /**
     * On login, copy settings from the player's {@link User} document
     * to their PlayerSettingManager.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void loadSettings(UserLoginEvent event) {
        new UserSettingManager(event.getUser()).copyTo(getManager(event.getPlayer()));
    }

    /**
     * Push setting changes to the API
     */
    private class Callback extends PlayerSettingCallback {
        @Override
        public void notifyChange(@Nonnull Player player, @Nonnull Setting setting, @Nullable Object oldValue, @Nullable Object newValue) {
            userService.changeSetting(userStore.getUser(player), new ChangeSettingRequest() {
                @Override public String profile() {
                    return localServer.settings_profile();
                }

                @Override public String setting() {
                    return setting.getName();
                }

                @Override public String value() {
                    return newValue == null ? null : setting.getType().serialize(newValue);
                }
            });
        }
    }

    /**
     * Read settings directly from a {@link User} document.
     *
     * This is read-only, write methods will throw an exception.
     */
    private class UserSettingManager extends AbstractSettingManager {

        final User user;

        UserSettingManager(User user) {
            this.user = user;
        }

        Map<String, String> profile() {
            return Optional.ofNullable(user.mc_settings_by_profile()
                                           .get(localServer.settings_profile()))
                           .orElseGet(ImmutableMap::of);
        }

        void copyTo(SettingManager that) {
            final Map<String, String> profile = profile();
            settingRegistry.getSettings().forEach(setting -> {
                that.setValue(setting, getRawValue(setting, profile), false);
            });
        }

        @Nullable Object getRawValue(Setting setting, Map<String, String> profile) {
            final String text = profile.get(setting.getName());
            if(text == null) return null;

            try {
                return setting.getType().parse(text);
            } catch(TypeParseException e) {
                return null;
            }
        }

        @Override
        public @Nullable Object getRawValue(Setting setting) {
            return getRawValue(setting, profile());
        }

        @Override
        protected void setRawValue(Setting setting, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SettingCallbackManager getCallbackManager() {
            throw new UnsupportedOperationException();
        }
    }
}
