package tc.oc.pgm.mutation.types.other;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.MutationModule;
import tc.oc.pgm.rage.RageMatchModule;

public class RageMutation extends MutationModule.Impl {

    RageMatchModule rage;

    public RageMutation(Match match) {
        super(match);
        this.rage = match.module(RageMatchModule.class).orElse(new RageMatchModule(match));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        rage.handlePlayerDamage(event);
    }

    @Override
    public void disable() {
        super.disable();
        rage = null;
    }

}
