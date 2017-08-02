package tc.oc.commons.bukkit.whisper;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.EnumType;
import me.anxuiz.settings.types.Name;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.nick.Familiarity;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.commons.bukkit.util.ItemCreator;

public class WhisperSettings {

    // Common values between private message settings
    private static final Options defaultValue = Options.ALL;
    private static final EnumType enumType = new EnumType("Private Message Options", Options.class);
    private static final String description = "Options:\n" +
            "ALL: everybody\n" +
            "FRIENDS: friends only\n" +
            "NONE: nobody";

    /**
     * Whom users can receive private messages from.
     */
    private static final Setting recieve = new SettingBuilder()
            .name("PrivateMessages")
            .alias("msg").alias("message").alias("messages").alias("pm").alias("pmr")
            .description(description).type(enumType).defaultValue(defaultValue)
            .summary("Who can send you private messages")
            .get();

    public static Setting receive() {
        return recieve;
    }

    // Permission that allows you to send to anyone
    public static final String SEND_OVERRIDE_PERMISSION = "projectares.msg.override";

    /**
     * Whether a user gets a sound notification when a private message arrives.
     */
    private static final Setting sound = new SettingBuilder()
            .name("PrivateMessageSounds")
            .alias("sounds").alias("pmsound").alias("pms")
            .description(description).type(enumType).defaultValue(defaultValue)
            .summary("Hear a sound when you get a message").get();

    public static Setting sound() {
        return sound;
    }

    public enum Options {
        @Name("all") ALL(Familiarity.PERSON),
        @Name("friends") FRIENDS(Familiarity.FRIEND),
        @Name("none") NONE(Familiarity.SELF);

        private final Familiarity minimumFamiliarity;

        Options(Familiarity minimumFamiliarity) {
            this.minimumFamiliarity = minimumFamiliarity;
        }

        public boolean isAllowed(Familiarity familiarity) {
            return familiarity.noLessThan(minimumFamiliarity);
        }

        public boolean canSend(CommandSender sender, Identity recipient) {
            return sender.hasPermission(SEND_OVERRIDE_PERMISSION) ||
                   isAllowed(recipient.familiarity(sender));
        }
    }
}
