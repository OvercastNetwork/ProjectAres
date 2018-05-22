package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import tc.oc.commons.core.chat.AbstractAudiences;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.chat.ConsoleAudience;
import tc.oc.commons.core.chat.NullAudience;
import tc.oc.commons.core.chat.Sound;
import tc.oc.commons.core.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

import static tc.oc.minecraft.protocol.MinecraftVersion.lessThan;
import static tc.oc.minecraft.protocol.MinecraftVersion.MINECRAFT_1_8;

@Singleton
public class Audiences extends AbstractAudiences<CommandSender> {

    @Inject
    ComponentRenderContext renderContext;

    @Override
    public Audience get(@Nullable CommandSender sender) {
        if(sender == null) {
            return NullAudience.INSTANCE;
        } if(sender instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            if(lessThan(MINECRAFT_1_8, player.getProtocolVersion())) {
                return new LegacyPlayer((org.bukkit.entity.Player) sender);
            } else {
                return new Player((org.bukkit.entity.Player) sender);
            }
        } else if(sender instanceof ConsoleCommandSender) {
            return new Console(sender.getServer().getConsoleSender());
        } else {
            return new Sender(sender);
        }
    }

    abstract class Base<T extends CommandSender> implements Audience {

        private final T sender;

        protected Base(T sender) {
            this.sender = sender;
        }

        protected T sender() {
            return sender;
        }

        @Override
        public void sendMessage(BaseComponent message) {
            sender().sendMessage(render(message));
        }

        @Override
        public void sendWarning(BaseComponent message, boolean audible) {
            sendMessage(new WarningComponent(message));
        }

        protected BaseComponent render(@Nullable BaseComponent component) {
            return component == null ? new Component() : renderContext.render(component, sender());
        }

    }

    class Sender extends Base<CommandSender> {

        public Sender(CommandSender sender) {
            super(sender);
        }

        @Override
        public void playSound(Sound sound) {}

        @Override
        public void stopSound(Sound sound) {}

        @Override
        public void sendHotbarMessage(BaseComponent message) {
            sendMessage(message);
        }

        @Override
        public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
            Optional.ofNullable(title).ifPresent(this::sendMessage);
            Optional.ofNullable(subtitle).ifPresent(this::sendMessage);
        }

        @Override
        public void hideTitle() {}

    }

    class Player extends Base<org.bukkit.entity.Player> {

        public Player(org.bukkit.entity.Player player) {
            super(player);
        }

        @Override
        public void sendHotbarMessage(BaseComponent message) {
            sender().sendMessage(ChatMessageType.ACTION_BAR, message);
        }

        @Override
        public void hideTitle() {
            sender().hideTitle();
        }

        @Override
        public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
            sender().showTitle(render(title), render(subtitle), inTicks, stayTicks, outTicks);
        }

        @Override
        public void playSound(Sound sound) {
            sender().playSound(sender().getLocation(), sound.name(), sound.volume(), sound.pitch());
        }

        @Override
        public void stopSound(Sound sound) {
            sender().stopSound(sound.name());
        }

    }

    class LegacyPlayer extends Player {

        private BaseComponent recentHotbarMessage;

        public LegacyPlayer(org.bukkit.entity.Player player) {
            super(player);
        }

        protected void emphasize(BaseComponent message) {
            sendMessage(Components.blank());
            sendMessage(message);
            sendMessage(Components.blank());
        }

        @Override
        public void sendHotbarMessage(BaseComponent message) {
            // Do not spam hot bar messages, as the protocol converts
            // them to regular chat messages.
            if(!Components.equals(message, recentHotbarMessage)) {
                emphasize(message);
                recentHotbarMessage = message;
            }
        }

        @Override
        public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
            emphasize(Components.join(Components.space(), Stream.of(title, subtitle).filter(msg -> msg != null).collect(Collectors.toImmutableList())));
        }
    }

    class Console extends Base<ConsoleCommandSender> implements ConsoleAudience {

        public Console(ConsoleCommandSender sender) {
            super(sender);
        }

    }

    public static class Deprecated {

        @Inject static Audiences audiences;

        @java.lang.Deprecated
        public static Audience get(@Nullable CommandSender sender) {
            return audiences.get(sender);
        }

    }

}
