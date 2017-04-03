package tc.oc.pgm.mutation.types.targetable;

import com.google.common.collect.Range;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.EntityMutation;
import tc.oc.pgm.mutation.types.TargetMutation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class BomberMutation extends EntityMutation<TNTPrimed> implements TargetMutation {

    final static Duration FREQUENCY = Duration.ofSeconds(30);
    final static Range<Integer> TARGETS = Range.closed(2, 5);
    final static Range<Integer> HEIGHT = Range.closed(30, 60);
    final static Range<Integer> TICKS = Range.closed(10, 30);

    Instant next;

    public BomberMutation(Match match) {
        super(match, false);
    }

    @Override
    public void target(List<MatchPlayer> players) {
        players.forEach(player -> {
            int bombs = entropy().randomInt(TARGETS);
            int height = entropy().randomInt(HEIGHT);
            Location location = player.getLocation().clone().add(0, height, 0);
            for(int i = 0; i < bombs; i++) {
                TNTPrimed tnt = spawn(location, TNTPrimed.class);
                tnt.setGlowing(true);
                tnt.setIsIncendiary(false);
                tnt.setFuseTicks(200);
                tnt.setVelocity(
                    new Vector(
                        (random().nextBoolean() ? .5 : -.5) * entropy().randomDouble(),
                        -entropy().randomDouble(),
                        (random().nextBoolean() ? .5 : -.5) * entropy().randomDouble()
                    )
                );
            }
        });
    }

    @Override
    public int targets() {
        return match().entropyForTick().randomInt(TARGETS);
    }

    @Override
    public Instant next() {
        return next;
    }

    @Override
    public void next(Instant time) {
        next = time;
    }

    @Override
    public Duration frequency() {
        return FREQUENCY;
    }

    @Override
    public void remove(TNTPrimed entity) {
        entity.setFuseTicks(entropy().randomInt(TICKS));
    }

    @Override
    public void enable() {
        super.enable();
        TargetMutation.super.enable();
    }

    @Override
    public void tick() {
        TargetMutation.super.tick();
        entities().filter(TNTPrimed::isOnGround).forEach(this::despawn);
    }

}
