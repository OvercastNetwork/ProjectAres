package tc.oc.pgm.match;

import javax.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import tc.oc.pgm.match.inject.MatchScoped;

@MatchScoped
public class Observers extends ObservingParty {

    @Inject public Observers(Match match) {
        super(match);
    }

    @Override
    public String getDefaultName() {
        return "Observers";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{match=" + getMatch() + "}";
    }
}
