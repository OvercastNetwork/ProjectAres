package tc.oc.pgm.stamina.symptoms;

import com.google.common.collect.Range;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import tc.oc.pgm.utils.NumericModifier;

public class ArcherySymptom extends StaminaSymptom {

    public final NumericModifier modifier;

    public ArcherySymptom(Range<Double> range, NumericModifier modifier) {
        super(range);
        this.modifier = modifier;
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event) {
        super.onShoot(event);

        if(event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            arrow.setDamage(modifier.apply(arrow.getDamage()));
            arrow.setVelocity(arrow.getVelocity().multiply(modifier.apply(1d)));
        }
    }
}
