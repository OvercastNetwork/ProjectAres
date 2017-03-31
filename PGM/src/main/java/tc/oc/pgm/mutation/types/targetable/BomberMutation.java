package tc.oc.pgm.mutation.types.targetable;

import com.google.common.collect.Range;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;
import tc.oc.commons.core.collection.WeakHashSet;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.mutation.types.TargetMutation;

import java.time.Duration;
import java.util.List;

public class BomberMutation extends TargetMutation {

    final static Duration FREQUENCY = Duration.ofSeconds(30);
    final static Range<Integer> TARGETS = Range.closed(1, 5);
    final static Range<Integer> HEIGHT = Range.closed(30, 60);
    final static Range<Integer> TICKS = Range.closed(10, 30);

    final WeakHashSet<TNTPrimed> falling;

    public BomberMutation(Match match) {
        super(match, FREQUENCY);
        this.falling = new WeakHashSet<>();
    }

    @Override
    public void execute(List<MatchPlayer> players) {
        players.forEach(player -> {
            int bombs = entropy.randomInt(TARGETS);
            int height = entropy.randomInt(HEIGHT);
            Location location = player.getLocation().clone().add(0, height, 0);
            for(int i = 0; i < bombs; i++) {
                TNTPrimed tnt = world.spawn(location, TNTPrimed.class);
                tnt.setGlowing(true);
                tnt.setIsIncendiary(false);
                tnt.setFuseTicks(Integer.MAX_VALUE);
                tnt.setVelocity(
                    new Vector(
                        (random.nextBoolean() ? .5 : -.5) * entropy.randomDouble(),
                        -entropy.randomDouble(),
                        (random.nextBoolean() ? .5 : -.5) * entropy.randomDouble()
                    )
                );
                falling.add(tnt);
            }
        });
    }

    @Override
    public int targets() {
        return match.entropyForTick().randomInt(TARGETS);
    }

    @Repeatable
    public void tick() {
        falling.stream()
               .filter(TNTPrimed::isOnGround)
               .forEach(tnt -> tnt.setFuseTicks(entropy.randomInt(TICKS)));
        falling.removeIf(TNTPrimed::isOnGround);
    }

}
