package tc.oc.pgm.kits;

import tc.oc.pgm.match.MatchPlayer;

public class NaturalRegenerationKit extends Kit.Impl {

    private final boolean fast, enabled;

    public NaturalRegenerationKit(boolean fast, boolean enabled) {
        this.fast = fast;
        this.enabled = enabled;
    }

    public void toggle(MatchPlayer player, boolean enabled) {
        if(fast) {
            player.getBukkit().setFastNaturalRegeneration(enabled);
        } else {
            player.getBukkit().setSlowNaturalRegeneration(enabled);
        }
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        toggle(player, enabled);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        toggle(player, !enabled);
    }

}
