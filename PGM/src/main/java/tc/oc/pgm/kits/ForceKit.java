package tc.oc.pgm.kits;

import org.bukkit.util.Vector;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.physics.AccelerationPlayerFacet;
import tc.oc.pgm.physics.PlayerForce;
import tc.oc.pgm.physics.RelativeFlags;

public class ForceKit extends Kit.Impl {

    private final PlayerForce playerForce;

    public ForceKit(Vector acceleration, RelativeFlags relative) {
        this.playerForce = new PlayerForce(acceleration, relative);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.facet(AccelerationPlayerFacet.class).addForce(playerForce);
    }

    @Override
    public void remove(MatchPlayer player) {
        player.facet(AccelerationPlayerFacet.class).removeForce(playerForce);
    }
}
