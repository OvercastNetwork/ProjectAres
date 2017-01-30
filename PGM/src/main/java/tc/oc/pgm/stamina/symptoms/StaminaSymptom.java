package tc.oc.pgm.stamina.symptoms;

import com.google.common.collect.Range;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import tc.oc.pgm.match.MatchPlayer;

public abstract class StaminaSymptom {
    public final Range<Double> range;

    public StaminaSymptom(Range<Double> range) {
        this.range = range;
    }

    public void apply(MatchPlayer player) {}
    public void remove(MatchPlayer player) {}

    public void onAttack(EntityDamageEvent event) {}
    public void onShoot(ProjectileLaunchEvent event) {}
    public void onSwing(PlayerAnimationEvent event) {}
}
