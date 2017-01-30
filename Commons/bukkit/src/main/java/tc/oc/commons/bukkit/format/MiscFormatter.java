package tc.oc.commons.bukkit.format;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Component;

public class MiscFormatter {

    public BaseComponent abled(boolean enabled) {
        return new Component(
            new TranslatableComponent(enabled ? "misc.enabled" : "misc.disabled"),
            enabled ? ChatColor.GREEN : ChatColor.RED
        );
    }

    public BaseComponent typePrefix(String text) {
        return new Component(ChatColor.WHITE)
            .extra("[")
            .extra(new Component(text, ChatColor.GOLD))
            .extra("] ");
    }

    public BaseComponent clickHere(ClickEvent.Action action, String value) {
        return new Component(
            new TranslatableComponent("misc.clickHere"),
            ChatColor.AQUA,
            ChatColor.BOLD
        ).clickEvent(action, value);
    }
}
