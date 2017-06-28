package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

public class GravityKit extends Kit.Impl {

    boolean gravity;

    public GravityKit(boolean gravity) {
        this.gravity = gravity;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setGravity(gravity);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getBukkit().setGravity(true);
    }
}
