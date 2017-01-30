package tc.oc.pgm.broadcast;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Sound;
import java.time.Duration;
import tc.oc.commons.bukkit.chat.BukkitSound;
import tc.oc.commons.bukkit.chat.TemplateComponent;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.TimeUtils;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.filters.Filter;

import static com.google.common.base.Preconditions.checkNotNull;

@FeatureInfo(name = "broadcast", plural = "broadcasts", singular = {"alert", "tip"})
public class Broadcast extends FeatureDefinition.Impl {

    public static final Duration MIN_INTERVAL = TimeUtils.fromTicks(1);

    public enum Type {
        TIP(new Component(new TranslatableComponent("prefixed.tip"), ChatColor.BLUE),
            new BukkitSound(Sound.ENTITY_ENDERMEN_AMBIENT, 1, 1.2f)),

        ALERT(new Component(new TranslatableComponent("prefixed.alert"), ChatColor.YELLOW),
              new BukkitSound(Sound.BLOCK_NOTE_PLING, 1, 2f));

        final BaseComponent prefix;
        final BukkitSound sound;

        Type(BaseComponent prefix, BukkitSound sound) {
            this.prefix = prefix;
            this.sound = sound;
        }

        public BaseComponent format(BaseComponent message) {
            return new Component(ChatColor.GRAY, ChatColor.BOLD)
                .extra("[")
                .extra(prefix)
                .extra("] ")
                .extra(new Component(message, ChatColor.AQUA).bold(false));
        }
    }

    @Inspect final Type type;
    @Inspect final Duration after;
    @Inspect final @Nullable Duration every;
    @Inspect final int count;
    @Inspect final MessageTemplate message;
    @Inspect final Filter filter;

    public Broadcast(Type type, Duration after, @Nullable Duration every, int count, MessageTemplate message, @Nullable Filter filter) {
        this.type = checkNotNull(type);
        this.after = checkNotNull(after);
        this.every = every;
        this.count = count;
        this.message = checkNotNull(message);
        this.filter = checkNotNull(filter);
    }

    public BaseComponent getFormattedMessage() {
        return type.format(new TemplateComponent(message));
    }

    public BukkitSound getSound() {
        return this.type.sound;
    }
}
