package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

import javax.annotation.Nullable;

public class HungerKit extends Kit.Impl {
    @Nullable protected final Float saturation;
    @Nullable protected final Integer foodLevel;

    public HungerKit(@Nullable Float saturation, @Nullable Integer foodLevel) {
        this.saturation = saturation;
        this.foodLevel = foodLevel;
    }

    /**
     * The force flag allows the kit to decrease the player's food levels
     */
    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        if(this.saturation != null && (force || player.getBukkit().getSaturation() < this.saturation)) {
            player.getBukkit().setSaturation(this.saturation);
        }

        if(this.foodLevel != null && (force || player.getBukkit().getFoodLevel() < this.foodLevel)) {
            player.getBukkit().setFoodLevel(this.foodLevel);
        }
    }

}
