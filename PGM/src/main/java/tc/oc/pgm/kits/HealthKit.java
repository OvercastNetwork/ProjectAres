package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

public class HealthKit extends Kit.Impl {
    protected final int halfHearts;

    public HealthKit(int halfHearts) {
        this.halfHearts = halfHearts;
    }

    /**
     * The force flag allows the kit to decrease the player's health
     */
    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        // Trying to set health > max throws an exception
        double newHealth = Math.min(halfHearts, player.getBukkit().getMaxHealth());
        if(force || player.getBukkit().getHealth() < newHealth) {
            player.getBukkit().setHealth(newHealth);
        }
    }
}
