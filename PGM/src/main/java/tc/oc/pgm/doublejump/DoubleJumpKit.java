package tc.oc.pgm.doublejump;

import java.time.Duration;
import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.MatchPlayer;

public class DoubleJumpKit extends Kit.Impl {
    public static final float DEFAULT_POWER = 3f; // mainly for backward compatibility
    public static final Duration DEFAULT_RECHARGE = Duration.ofMillis(2500);

    protected final boolean enabled;
    protected final float power;                // 1 power is roughly a normal vanilla jump
    protected final Duration rechargeTime;
    protected final boolean rechargeInAir;

    public DoubleJumpKit(boolean enabled, float power, Duration rechargeTime, boolean rechargeInAir) {
        this.enabled = enabled;
        this.power = power;
        this.rechargeTime = rechargeTime;
        this.rechargeInAir = rechargeInAir;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        DoubleJumpMatchModule djmm = player.getMatch().getMatchModule(DoubleJumpMatchModule.class);
        if(djmm != null) djmm.setKit(player.getBukkit(), this);
    }

    public float chargePerTick() {
        return 50F / this.rechargeTime.toMillis();
    }

    public boolean needsRecharge() {
        return !Duration.ZERO.equals(this.rechargeTime);
    }
}
