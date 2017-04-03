package tc.oc.pgm.mutation.types.kit;

import org.bukkit.Material;
import tc.oc.pgm.gamerules.GameRulesMatchModule;
import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.KitMutation;
import tc.oc.pgm.shield.ShieldKit;
import tc.oc.pgm.shield.ShieldParameters;

public class HardcoreMutation extends KitMutation {

    final static String KEY = "naturalRegeneration";
    final static ShieldKit SHIELD = new ShieldKit(new ShieldParameters());
    final static ItemKit APPLE = new FreeItemKit(item(Material.GOLDEN_APPLE));

    String previous; // Stores the previous game rule setting

    public HardcoreMutation(Match match) {
        super(match, false);
        this.kits.add(SHIELD);
        this.rewards.add(new KillReward(APPLE));
    }

    public GameRulesMatchModule rules() {
        return match().module(GameRulesMatchModule.class).get();
    }

    @Override
    public void enable() {
        super.enable();
        previous = world().getGameRuleValue(KEY);
        rules().gameRules().put(KEY, "false");
    }

    @Override
    public void disable() {
        rules().gameRules().remove(KEY);
        if(previous != null) {
            world().setGameRuleValue(KEY, previous);
        }
        super.disable();
    }

}
