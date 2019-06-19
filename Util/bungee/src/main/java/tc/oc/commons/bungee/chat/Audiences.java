package tc.oc.commons.bungee.chat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tc.oc.commons.core.chat.AbstractAudiences;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.ConsoleAudience;
import tc.oc.commons.core.chat.Sound;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
public class Audiences extends AbstractAudiences<CommandSender> {

    @Override
    public Audience get(CommandSender sender) {
        if(sender instanceof ProxiedPlayer) {
            return new Player((ProxiedPlayer) sender);
        } else {
            return new Console();
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
            sender().sendMessage(message);
        }

        @Override
        public void sendWarning(BaseComponent message, boolean audible) {
            sendMessage(message);
        }

    }

    class Player extends Base<ProxiedPlayer> {

        public Player(ProxiedPlayer proxiedPlayer) {
            super(proxiedPlayer);
        }

        @Override
        public void playSound(Sound sound) {}

        @Override
        public void stopSound(Sound sound) {}

        @Override
        public void sendHotbarMessage(BaseComponent message) {
            sender().sendMessage(ChatMessageType.ACTION_BAR, message);
        }

        @Override
        public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
            sender().sendTitle(ProxyServer.getInstance().createTitle().title(title).subTitle(subtitle).fadeIn(inTicks).stay(stayTicks).fadeOut(outTicks));
        }

        @Override
        public void hideTitle() {
            sender().sendTitle(ProxyServer.getInstance().createTitle().clear());
        }

    }

    class Console implements ConsoleAudience {

        @Override
        public void sendMessage(BaseComponent message) {
            ProxyServer.getInstance().getConsoleSender().sendMessage(message);
        }

    }

}
