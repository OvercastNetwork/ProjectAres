package tc.oc.commons.bukkit.settings;

import com.google.inject.Binder;
import me.anxuiz.settings.Setting;
import tc.oc.commons.core.inject.SetBinder;

/**
 * Used to register {@link Setting}s
 */
public class SettingBinder extends SetBinder<Setting> {
    public SettingBinder(Binder binder) {
        super(binder);
    }
}
