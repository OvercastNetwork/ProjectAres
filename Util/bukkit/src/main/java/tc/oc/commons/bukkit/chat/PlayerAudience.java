package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Sound;

public class PlayerAudience extends CommandSenderAudience {

    public PlayerAudience(Player player) {
        super(player);
    }

    protected Player getPlayer() {
        return (Player) getCommandSender();
    }

    @Override
    public void sendHotbarMessage(BaseComponent message) {
        getPlayer().sendMessage(ChatMessageType.ACTION_BAR, ComponentRenderers.render(message, getPlayer()));
    }

    @Override
    public void showTitle(BaseComponent title, BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        title = title == null ? new Component("") : ComponentRenderers.render(title, getPlayer());
        subtitle = subtitle == null ? new Component("") : ComponentRenderers.render(subtitle, getPlayer());
        getPlayer().showTitle(title, subtitle, inTicks, stayTicks, outTicks);
    }

    @Override
    public void hideTitle() {
        getPlayer().hideTitle();
    }

    @Override
    public void playSound(Sound sound) {
        getPlayer().playSound(getPlayer().getLocation(), sound.name(), sound.volume(), sound.pitch());
    }
}
