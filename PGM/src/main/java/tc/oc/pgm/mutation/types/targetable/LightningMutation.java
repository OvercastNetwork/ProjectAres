package tc.oc.pgm.mutation.types.targetable;

import com.google.common.collect.Range;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.TargetMutation;

import java.time.Duration;
import java.util.List;

public class LightningMutation extends TargetMutation.Impl {

    final static Duration FREQUENCY = Duration.ofSeconds(30);
    final static Range<Integer> TARGETS = Range.closed(2, 5);
    final static Range<Integer> STRIKES = Range.closed(1, 3);

    public LightningMutation(Match match) {
        super(match, FREQUENCY);
    }

    @Override
    public void target(List<MatchPlayer> players) {
        players.forEach(player -> {
            Location location = player.getLocation();
            world().strikeLightning(location.clone().add(Vector.getRandom()));
            int strikes = entropy().randomInt(STRIKES);
            for(int i = 0; i < strikes; i++) {
                world().strikeLightningEffect(location.clone().add(Vector.getRandom().multiply(Math.pow(i + 1, 2))));
            }
        });
    }

    @Override
    public int targets() {
        return match().entropyForTick().randomInt(TARGETS);
    }

}
