package tc.oc.pgm.mutation.types.kit;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.kits.PotionKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.stream.Stream;

public class StealthMutation extends KitMutation {

    final static PotionKit INVISIBILITY = new PotionKit(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));

    public StealthMutation(Match match) {
        super(match, true, INVISIBILITY);
    }

    @Override
    public Stream<? extends Slot> saved() {
        return Slot.Armor.armor();
    }

}
