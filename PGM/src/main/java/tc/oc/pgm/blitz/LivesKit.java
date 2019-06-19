package tc.oc.pgm.blitz;

import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.MatchPlayer;

public class LivesKit extends Kit.Impl {

    private final int lives;

    public LivesKit(int lives) {
        this.lives = lives;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getMatch().module(BlitzMatchModuleImpl.class).ifPresent(blitz -> blitz.increment(player, lives, true, force));
    }

}
