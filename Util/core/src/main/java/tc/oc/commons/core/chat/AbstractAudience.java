package tc.oc.commons.core.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class AbstractAudience implements Audience {

    @Override
    public void sendWarning(BaseComponent message, boolean audible) {
        sendMessage(new Component(ChatColor.RED).extra(new Component(" \u26a0 ", ChatColor.YELLOW), message));
    }

    @Override
    public void sendWarning(String message, boolean audible) {
        sendMessage(ChatColor.YELLOW + " \u26a0 " + ChatColor.RED + message);
    }
}
