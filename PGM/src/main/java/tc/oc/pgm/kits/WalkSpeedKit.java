package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

public class WalkSpeedKit extends Kit.Impl {
    public static final float MIN = 0, MAX = 5;
    public static final float BUKKIT_DEFAULT = 0.2f;

    private final float speedMultiplier;

    public WalkSpeedKit(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getBukkit().setWalkSpeed(BUKKIT_DEFAULT * this.speedMultiplier);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getBukkit().setWalkSpeed(BUKKIT_DEFAULT);
    }
}
