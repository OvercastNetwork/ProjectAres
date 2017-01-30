package tc.oc.pgm.stamina.symptoms;

import com.google.common.collect.Range;
import org.bukkit.potion.PotionEffectType;

public class PotionSymptom extends StaminaSymptom {
    public final PotionEffectType effect;
    public final int amplifier;

    public PotionSymptom(Range<Double> range, PotionEffectType effect, int amplifier) {
        super(range);
        this.effect = effect;
        this.amplifier = amplifier;
    }
}
