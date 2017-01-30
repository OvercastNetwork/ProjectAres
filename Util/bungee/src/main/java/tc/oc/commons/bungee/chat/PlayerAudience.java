package tc.oc.commons.bungee.chat;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tc.oc.commons.core.chat.AbstractAudience;
import tc.oc.commons.core.chat.Sound;

public class PlayerAudience extends AbstractAudience {

    private final ProxiedPlayer player;

    public PlayerAudience(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public void sendMessage(BaseComponent message) {
        player.sendMessage(message);
    }

    @Override
    public void playSound(Sound sound) {
        // Possible, but not worth the trouble
    }

    @Override
    public void sendHotbarMessage(BaseComponent message) {
        player.sendMessage(ChatMessageType.ACTION_BAR, message);
    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        player.sendTitle(ProxyServer.getInstance().createTitle().title(title).subTitle(subtitle).fadeIn(inTicks).stay(stayTicks).fadeOut(outTicks));
    }

    @Override
    public void hideTitle() {
        player.sendTitle(ProxyServer.getInstance().createTitle().clear());
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }
}
