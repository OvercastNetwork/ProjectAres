package tc.oc.pgm.tablist;

import java.util.Comparator;

import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.nick.PlayerOrder;
import tc.oc.pgm.PGM;
import tc.oc.pgm.match.MatchPlayer;

public class MatchPlayerOrder implements Comparator<MatchPlayer> {

    public interface Factory {
        MatchPlayerOrder create(MatchPlayer viewer);
    }

    private final PlayerOrder playerOrder;

    public MatchPlayerOrder(CommandSender viewer) {
        this.playerOrder = PGM.get().injector().getInstance(PlayerOrder.Factory.class).apply(viewer);
    }

    @Override
    public int compare(MatchPlayer a, MatchPlayer b) {
        return playerOrder.compare(a.getBukkit(), b.getBukkit());
    }
}
