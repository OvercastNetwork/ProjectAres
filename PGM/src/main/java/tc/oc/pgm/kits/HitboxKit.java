package tc.oc.pgm.kits;

import tc.oc.pgm.damage.HitboxPlayerFacet;
import tc.oc.pgm.match.MatchPlayer;

public class HitboxKit extends Kit.Impl {

    private final double width;

    public HitboxKit(double width) {
        this.width = width;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.facet(HitboxPlayerFacet.class).setWidth(width);
    }
}
