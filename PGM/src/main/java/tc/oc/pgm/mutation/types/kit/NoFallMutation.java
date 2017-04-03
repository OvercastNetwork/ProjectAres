package tc.oc.pgm.mutation.types.kit;

import org.bukkit.event.entity.EntityDamageEvent;
import tc.oc.pgm.damage.DisableDamageMatchModule;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.PlayerRelation;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class NoFallMutation extends KitMutation {

    final static EntityDamageEvent.DamageCause FALL = EntityDamageEvent.DamageCause.FALL;
    final static Iterable<PlayerRelation> RELATIONS = Stream.of(PlayerRelation.values()).collect(Collectors.toList());

    Iterable<PlayerRelation> previous;

    public NoFallMutation(Match match, boolean force, Kit... kits) {
        super(match, force, kits);
    }

    public DisableDamageMatchModule damage() {
        return match().module(DisableDamageMatchModule.class).get();
    }

    @Override
    public void enable() {
        super.enable();
        previous = damage().causes().get(FALL);
        damage().causes().putAll(FALL, RELATIONS);
    }

    @Override
    public void disable() {
        damage().causes().removeAll(FALL);
        if(previous != null) {
            damage().causes().putAll(FALL, previous);
        }
        super.disable();
    }

}
