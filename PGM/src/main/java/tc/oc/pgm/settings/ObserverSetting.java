package tc.oc.pgm.settings;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.EnumType;
import me.anxuiz.settings.types.Name;
import org.bukkit.Material;
import tc.oc.commons.bukkit.util.ItemCreator;

public class ObserverSetting {
    private static final Setting inst = new SettingBuilder()
        .name("Observers").alias("obs")
        .summary("See other observers while spectating")
        .description("Options:\n" +
                "ALL: show all observers\n" +
                "FRIENDS: show friend observers\n" +
                "NONE: show no observers")
        .type(new EnumType<Options>("Observer Options", Options.class))
        .defaultValue(Options.ALL).get();

    public static Setting get() {
        return inst;
    }

    public enum Options {
        @Name("all")
        ALL,

        @Name("friends")
        FRIENDS,

        @Name("none")
        NONE
    }
}
