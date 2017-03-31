package tc.oc.pgm.mutation.types.kit;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.pgm.kits.PotionKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.KitMutation;

public class GlowMutation extends KitMutation {

    final static PotionKit GLOW = new PotionKit(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));

    public GlowMutation(Match match) {
        super(match, true, GLOW);
    }

}
