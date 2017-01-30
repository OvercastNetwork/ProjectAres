package tc.oc.commons.bukkit.settings;

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingCallback;

/**
 * Used to register {@link SettingCallback}s for specific {@link Setting}s
 */
public class SettingCallbackBinder {

    private final MapBinder<Setting, SettingCallback> mapBinder;

    public SettingCallbackBinder(Binder binder) {
        mapBinder = MapBinder.newMapBinder(binder, Setting.class, SettingCallback.class)
                             .permitDuplicates();
    }

    public LinkedBindingBuilder<SettingCallback> changesIn(Setting setting) {
        return mapBinder.addBinding(setting);
    }
}
