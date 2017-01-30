package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

import static com.google.common.base.Preconditions.checkArgument;

public class MaxHealthKit extends Kit.Impl {

    private final double maxHealth;

    public MaxHealthKit(double maxHealth) {
        checkArgument(maxHealth > 0, "max health must be greater than zero");
        this.maxHealth = maxHealth;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setMaxHealth(maxHealth);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getBukkit().setMaxHealth(20);
    }
}
