package tc.oc.pgm.tablist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.bukkit.tablist.DynamicTabEntry;
import tc.oc.commons.bukkit.tablist.TabView;
import tc.oc.pgm.teams.Team;
import tc.oc.commons.core.util.DefaultProvider;

public class TeamTabEntry extends DynamicTabEntry {

    public static class Factory implements DefaultProvider<Team, TeamTabEntry> {
        @Override
        public TeamTabEntry get(Team key) {
            return new TeamTabEntry(key);
        }
    }

    private final Team team;

    protected TeamTabEntry(Team team) {
        this.team = team;
    }

    @Override
    public BaseComponent getContent(TabView view) {
        return new Component(
            new Component(String.valueOf(team.getPlayers().size()), ChatColor.WHITE),
            new Component("/", ChatColor.DARK_GRAY),
            new Component(String.valueOf(team.getMaxPlayers()), ChatColor.GRAY),
            new Component(" " + team.getShortName(), team.getColor(), ChatColor.BOLD)
        );
    }
}
