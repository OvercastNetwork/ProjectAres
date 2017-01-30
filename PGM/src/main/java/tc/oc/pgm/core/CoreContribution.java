package tc.oc.pgm.core;

import org.bukkit.Material;
import tc.oc.pgm.goals.Contribution;
import tc.oc.pgm.match.MatchPlayerState;

public class CoreContribution extends Contribution {

    private final Material material;

    public CoreContribution(MatchPlayerState playerState, double percentage, Material material) {
        super(playerState, percentage);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

}
