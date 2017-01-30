package tc.oc.pgm.kits;

import java.util.stream.Stream;

import tc.oc.pgm.match.MatchPlayer;

public class RemoveKit extends Kit.Impl {
    private final Kit kit;

    public RemoveKit(Kit kit) {
        this.kit = kit;
    }

    @Override
    public Stream<? extends Kit> dependencies() {
        return Stream.of(kit);
    }

    public Kit getKit() {
        return kit;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        kit.remove(player);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.facet(KitPlayerFacet.class).applyKit(kit, false);
    }
}
