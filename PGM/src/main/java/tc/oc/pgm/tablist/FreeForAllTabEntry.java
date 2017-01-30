package tc.oc.pgm.tablist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.bukkit.tablist.DynamicTabEntry;
import tc.oc.commons.bukkit.tablist.TabView;
import tc.oc.commons.core.util.DefaultProvider;
import tc.oc.pgm.match.Match;

public class FreeForAllTabEntry extends DynamicTabEntry {

    public static class Factory implements DefaultProvider<Match, FreeForAllTabEntry> {
        @Override
        public FreeForAllTabEntry get(Match key) {
            return new FreeForAllTabEntry(key);
        }
    }

    private final Match match;

    public FreeForAllTabEntry(Match match) {
        this.match = match;
    }

    @Override
    public BaseComponent getContent(TabView view) {
        return new Component(
            new Component(String.valueOf(match.getParticipatingPlayers().size()), ChatColor.WHITE),
            new Component("/", ChatColor.DARK_GRAY),
            new Component(String.valueOf(match.getMaxPlayers()), ChatColor.GRAY),
            new Component(" ", ChatColor.YELLOW, ChatColor.BOLD).extra(new TranslatableComponent("command.match.matchInfo.players"))
        );
    }
}
