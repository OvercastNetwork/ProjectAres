package tc.oc.commons.bukkit.broadcast;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.bukkit.broadcast.model.BroadcastPrefix;
import tc.oc.commons.core.chat.Component;

public class BroadcastFormatter {

    private static final Map<BroadcastPrefix, ChatColor> COLORS = ImmutableMap.<BroadcastPrefix, ChatColor>builder()
        .put(BroadcastPrefix.TIP, ChatColor.BLUE)
        .put(BroadcastPrefix.NEWS, ChatColor.YELLOW)
        .put(BroadcastPrefix.ALERT, ChatColor.RED)
        .put(BroadcastPrefix.INFO, ChatColor.LIGHT_PURPLE)
        .put(BroadcastPrefix.FACT, ChatColor.GOLD)
        .put(BroadcastPrefix.CHAT, ChatColor.GREEN)
        .build();

    public BaseComponent broadcast(BroadcastPrefix prefix, BaseComponent content) {
        return new Component(ChatColor.GRAY, ChatColor.BOLD)
            .extra("[")
            .extra(new Component(new TranslatableComponent("prefixed." + prefix.name().toLowerCase()), COLORS.get(prefix)))
            .extra("] ")
            .extra(new Component(content, ChatColor.AQUA).bold(false));
    }
}
