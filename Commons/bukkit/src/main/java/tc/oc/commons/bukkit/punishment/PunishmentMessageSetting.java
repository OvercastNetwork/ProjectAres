package tc.oc.commons.bukkit.punishment;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.EnumType;
import me.anxuiz.settings.types.Name;
import org.bukkit.Material;
import tc.oc.commons.bukkit.util.ItemCreator;

public class PunishmentMessageSetting {

    private static final Setting setting = new SettingBuilder()
        .name("PunishmentMessages").alias("punishments").alias("pmessages").alias("pmsgs")
        .summary("Punishment messages shown to you")
        .description("Options:\n" +
                "GLOBAL: punishments from all servers\n" +
                "SERVER: punishments from the current server\n" +
                "NONE: no messages\n")
        .type(new EnumType<>("Punishment Message Options", Options.class))
        .defaultValue(Options.SERVER)
        .get();

    public static Setting get() {
        return setting;
    }

    public enum Options {
        @Name("global") GLOBAL,
        @Name("server") SERVER,
        @Name("none")   NONE
    }

}
