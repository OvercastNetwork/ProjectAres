package tc.oc.pgm.picker;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;

public class PickerSettings {
    private PickerSettings() {}

    public static final Setting PICKER = new SettingBuilder()
        .name("Picker").alias("pkr")
        .summary("Get a GUI for choosing teams")
        .type(new BooleanType())
        .defaultValue(true).get();
}
