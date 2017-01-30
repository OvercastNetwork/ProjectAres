package tc.oc.pgm.shield;

import tc.oc.pgm.kits.ItemKitApplicator;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.MatchPlayer;

public class ShieldKit extends Kit.Impl {

    final ShieldParameters parameters;

    public ShieldKit(ShieldParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.getMatch().needMatchModule(ShieldMatchModule.class).applyShield(player, parameters);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getMatch().needMatchModule(ShieldMatchModule.class).removeShield(player);
    }
}
