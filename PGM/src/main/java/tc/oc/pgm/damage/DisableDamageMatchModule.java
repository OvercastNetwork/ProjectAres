package tc.oc.pgm.damage;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;
import tc.oc.pgm.match.PlayerRelation;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.mutation.MutationMatchModule;
import tc.oc.pgm.tracker.BlockResolver;
import tc.oc.pgm.tracker.EventResolver;
import tc.oc.pgm.tracker.damage.DamageInfo;

@ListenerScope(MatchScope.RUNNING)
public class DisableDamageMatchModule extends MatchModule implements Listener {

    @Inject private EventResolver eventResolver;
    @Inject private BlockResolver blockResolver;

    private final SetMultimap<DamageCause, PlayerRelation> causes;

    public DisableDamageMatchModule(SetMultimap<DamageCause, PlayerRelation> causes) {
        this.causes = causes;
    }

    public SetMultimap<DamageCause, PlayerRelation> causes() {
        return causes;
    }

    public ImmutableSetMultimap<DamageCause, PlayerRelation> causesImmutable() {
        return ImmutableSetMultimap.copyOf(causes());
    }

    private static DamageCause getBlockDamageCause(Block block) {
        switch(block.getType()) {
            case LAVA:
            case STATIONARY_LAVA:
                return DamageCause.LAVA;

            case FIRE:
                return DamageCause.FIRE;

            default:
                return DamageCause.CONTACT;
        }
    }

    private boolean canDamage(DamageCause cause, MatchPlayer victim, @Nullable ParticipantState damager) {
        return !causesImmutable().containsEntry(cause, PlayerRelation.get(victim.getParticipantState(), damager));
    }

    private boolean canDamage(DamageCause cause, MatchPlayer victim, DamageInfo info) {
        return !causesImmutable().containsEntry(cause, PlayerRelation.get(victim.getParticipantState(), info.getAttacker()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void handleIgnition(EntityCombustByBlockEvent event) {
        MatchPlayer victim = getMatch().getParticipant(event.getEntity());
        if(victim == null) return;

        ParticipantState attacker = blockResolver.getOwner(event.getCombuster());

        // Disabling FIRE/LAVA damage also prevents setting on fire
        if(!this.canDamage(getBlockDamageCause(event.getCombuster()), victim, attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void handleDamage(EntityDamageEvent event) {
        MatchPlayer victim = getMatch().getParticipant(event.getEntity());
        if(victim == null) return;

        DamageInfo damageInfo = eventResolver.resolveDamage(event);
        if(!canDamage(event.getCause(), victim, damageInfo)) {
            event.setCancelled(true);
        }
    }
}
