package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Material;
import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.HealthKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.MaxHealthKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.KitMutation;

public class HealthMutation extends KitMutation {

    final static HealthKit HEALTH = new HealthKit(40);
    final static MaxHealthKit MAX_HEALTH = new MaxHealthKit(40);
    final static ItemKit APPLE = new FreeItemKit(item(Material.GOLDEN_APPLE, 3));

    public HealthMutation(Match match) {
        super(match, false, MAX_HEALTH, HEALTH, APPLE);
        this.rewards.add(new KillReward(APPLE));
    }

}
