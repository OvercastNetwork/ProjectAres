package tc.oc.pgm.mutation.types.other;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import tc.oc.commons.bukkit.item.ItemUtils;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.MutationModule;

public class RageMutation extends MutationModule.Impl {

    public RageMutation(Match match) {
        super(match);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player && ItemUtils.isWeapon(((Player) event.getDamager()).getItemInHand())) {
            event.setDamage(1000);
        } else if(event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
            event.setDamage(1000);
        }
    }

    @Override
    public void disable() {
        super.disable();
    }

}
