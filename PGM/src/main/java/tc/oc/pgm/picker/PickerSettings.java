package tc.oc.pgm.picker;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Material;
import tc.oc.commons.bukkit.util.ItemCreator;

public class PickerSettings {
    private PickerSettings() {}

    public static final Setting PICKER = new SettingBuilder()
        .name("Picker").alias("pkr")
        .summary("Get a GUI for choosing teams")
        .type(new BooleanType())
        .defaultValue(true).get();
}
