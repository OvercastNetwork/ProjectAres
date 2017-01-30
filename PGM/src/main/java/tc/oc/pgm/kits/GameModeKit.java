package tc.oc.pgm.kits;

import org.bukkit.GameMode;
import tc.oc.pgm.match.MatchPlayer;

public class GameModeKit extends Kit.Impl {

    private final GameMode gameMode;

    public GameModeKit(GameMode mode) {
        gameMode = mode;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setGameMode(gameMode);
    }
}
