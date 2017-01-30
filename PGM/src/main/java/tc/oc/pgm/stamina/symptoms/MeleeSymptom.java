package tc.oc.pgm.stamina.symptoms;

import com.google.common.collect.Range;
import org.bukkit.event.entity.EntityDamageEvent;
import tc.oc.pgm.utils.NumericModifier;

public class MeleeSymptom extends StaminaSymptom {

    public final NumericModifier modifier;

    public MeleeSymptom(Range<Double> range, NumericModifier modifier) {
        super(range);
        this.modifier = modifier;
    }

    @Override
    public void onAttack(EntityDamageEvent event) {
        super.onAttack(event);

        if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setDamage(modifier.apply(event.getDamage()));
        }
    }
}
