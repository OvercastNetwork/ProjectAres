package tc.oc.pgm.death;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.TypeParseException;
import me.anxuiz.settings.types.BooleanType;
import org.bukkit.Material;
import tc.oc.commons.bukkit.util.ItemCreator;

public class HighlightDeathMessageSetting {
    private static final Setting INSTANCE = new SettingBuilder()
        .name("HighlightDeathMessages").alias("hdms").alias("hdm")
        .summary("Highlight your death messages")
        .type(new BooleanType() {
            @Override
            public Object parse(String raw) throws TypeParseException {
                try {
                    return super.parse(raw);
                } catch(TypeParseException e) {
                    return !"none".equals(raw); // Handle legacy values
                }
            }
        })
        .defaultValue(true)
        .get();

    public static Setting get() {
        return INSTANCE;
    }
}
