package tc.oc.pgm.death;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.EnumType;
import me.anxuiz.settings.types.Name;
import org.bukkit.Material;
import tc.oc.commons.bukkit.nick.Familiarity;
import tc.oc.commons.bukkit.util.ItemCreator;

public class DeathMessageSetting {
    private static final Setting inst = new SettingBuilder()
        .name("DeathMessages").alias("dms").alias("dm")
        .summary("Death messages displayed to you")
        .description("Options:\n" +
                     "ALL: show all death messages\n" +
                     "FRIENDS: show friend death messages\n" +
                     "OWN: show own death messages\n")
        .type(new EnumType<>("Death Message Options", Options.class))
        .defaultValue(Options.FRIENDS).get();

    public static Setting get() {
        return inst;
    }

    public enum Options {
        @Name("all") ALL(Familiarity.PERSON),
        @Name("friends") FRIENDS(Familiarity.FRIEND),
        @Name("own") OWN(Familiarity.SELF);

        private final Familiarity minimumFamiliarity;

        Options(Familiarity minimumFamiliarity) {
            this.minimumFamiliarity = minimumFamiliarity;
        }

        public boolean isAllowed(Familiarity familiarity) {
            return familiarity.noLessThan(minimumFamiliarity);
        }

    }
}
