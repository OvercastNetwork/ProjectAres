package tc.oc.pgm.tablist;

import java.util.List;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.bukkit.chat.ListComponent;
import tc.oc.commons.bukkit.chat.NameStyle;
import tc.oc.commons.bukkit.tablist.DynamicTabEntry;
import tc.oc.commons.bukkit.tablist.TabView;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.util.DefaultProvider;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.match.Match;

public class MapTabEntry extends DynamicTabEntry {

    public static class Factory implements DefaultProvider<Match, MapTabEntry> {
        @Override
        public MapTabEntry get(Match key) {
            return new MapTabEntry(key);
        }
    }

    private final Match match;

    protected MapTabEntry(Match match) {
        this.match = match;
    }

    @Override
    public BaseComponent getContent(TabView view) {
        BaseComponent content = new Component(match.getMapInfo().name, ChatColor.AQUA, ChatColor.BOLD);

        List<Contributor> authors = match.getMapInfo().getNamedAuthors();
        if(!authors.isEmpty()) {
            content = new Component(new TranslatableComponent(
                "misc.authorship",
                content,
                new ListComponent(Lists.transform(
                    authors,
                    contributor -> contributor.getStyledName(NameStyle.MAPMAKER)
                ))
            ), ChatColor.DARK_GRAY);
        }

        return content;
    }
}
