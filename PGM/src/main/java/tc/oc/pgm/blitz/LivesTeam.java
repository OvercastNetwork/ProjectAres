package tc.oc.pgm.blitz;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.PlayerId;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.pgm.match.Competitor;

public class LivesTeam extends LivesBase {

    public LivesTeam(Competitor competitor, int lives) {
        super(lives, competitor);
    }

    @Override
    public Type type() {
        return Type.TEAM;
    }

    @Override
    public boolean applicableTo(PlayerId player) {
        return competitor().players().anyMatch(matchPlayer -> matchPlayer.getPlayerId().equals(player));
    }

    @Override
    public boolean owner(PlayerId playerId) {
        return false;
    }

    public int alive() {
        return (int) competitor().participants().count();
    }

    @Override
    public BaseComponent remaining() {
        int alive = alive() - 1;
        if(alive == 0) return Components.blank();
        return empty() ? new Component(
            new TranslatableComponent(
                "lives.remaining.alive." + (alive == 1 ? "singular"
                                                       : "plural"),
                new Component(alive, ChatColor.YELLOW)
            ),
            ChatColor.AQUA
        ) : super.remaining();
    }

    @Override
    public int hashCode() {
        return competitor().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
               obj instanceof LivesTeam &&
               competitor().equals(((LivesTeam) obj).competitor());
    }

}
