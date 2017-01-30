package tc.oc.pgm.kits;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import tc.oc.pgm.match.MatchPlayer;

import javax.annotation.Nullable;

public class FlyKit extends Kit.Impl {
    public static final float MIN = 0, MAX = 10;
    public static final float BASE_INTERNAL_SPEED = 0.1f;

    protected final boolean allowFlight;
    protected final @Nullable Boolean flying;
    protected final float flySpeedMultiplier;


    public FlyKit(boolean allowFlight, @Nullable Boolean flying, float flySpeedMultiplier) {
        Preconditions.checkArgument(flying == null || !(flying && !allowFlight), "Flying cannot be true if allow-flight is false");

        this.allowFlight = allowFlight;
        this.flying = flying;
        this.flySpeedMultiplier = flySpeedMultiplier;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setAllowFlight(this.allowFlight);
        if(this.flying != null) {
            player.getBukkit().setFlying(this.flying);
        }

        player.getBukkit().setFlySpeed(BASE_INTERNAL_SPEED * flySpeedMultiplier);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        if(allowFlight) {
            player.getBukkit().setAllowFlight(false);
        }
        if(flying != null) {
            player.getBukkit().setFlying(!flying);
        }
        if(flySpeedMultiplier != 1) {
            player.getBukkit().setFlySpeed(BASE_INTERNAL_SPEED);
        }
    }
}
