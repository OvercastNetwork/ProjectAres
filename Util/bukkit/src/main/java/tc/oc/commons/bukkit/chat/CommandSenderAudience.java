package tc.oc.commons.bukkit.chat;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.AbstractAudience;
import tc.oc.commons.core.chat.Sound;

public class CommandSenderAudience extends AbstractAudience {

    protected final CommandSender sender;

    public CommandSenderAudience(CommandSender sender) {
        this.sender = sender;
    }

    protected CommandSender getCommandSender() {
        return sender;
    }

    @Override
    public void sendMessage(String message) {
        getCommandSender().sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent message) {
        ComponentRenderers.send(getCommandSender(), message);
    }

    @Override
    public void sendHotbarMessage(BaseComponent message) {
        sendMessage(message);
    }

    @Override
    public void showTitle(@Nullable BaseComponent title, @Nullable BaseComponent subtitle, int inTicks, int stayTicks, int outTicks) {
        if(title != null) sendMessage(title);
        if(subtitle != null) sendMessage(subtitle);
    }

    @Override
    public void hideTitle() {
    }

    @Override
    public void playSound(Sound sound) {
    }
}
