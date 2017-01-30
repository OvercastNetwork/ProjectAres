package tc.oc.pgm.kits;

import tc.oc.pgm.join.JoinMatchModule;
import tc.oc.pgm.match.MatchPlayer;

public class EliminateKit extends DelayedKit {

    @Override
    public void applyDelayed(MatchPlayer player, boolean force) {
        player.getMatch().needMatchModule(JoinMatchModule.class).observe(player);
    }

}
