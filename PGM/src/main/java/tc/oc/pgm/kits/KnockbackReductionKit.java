package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

public class KnockbackReductionKit extends Kit.Impl {
    private final float knockbackReduction;

    public KnockbackReductionKit(float reduction) {
        this.knockbackReduction = reduction;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setKnockbackReduction(this.knockbackReduction);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getBukkit().setKnockbackReduction(0);
    }
}
