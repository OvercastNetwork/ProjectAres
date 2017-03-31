package tc.oc.pgm.mutation.types.targetable;

import com.google.common.collect.Range;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.TargetMutation;

import java.time.Duration;
import java.util.List;

public class LightningMutation extends TargetMutation {

    final static Duration FREQUENCY = Duration.ofSeconds(30);
    final static Range<Integer> TARGETS = Range.closed(1, 5);
    final static Range<Integer> STRIKES = Range.closed(0, 3);

    public LightningMutation(Match match) {
        super(match, FREQUENCY);
    }

    @Override
    public void execute(List<MatchPlayer> players) {
        players.forEach(player -> {
            World world = match.getWorld();
            Location location = player.getLocation();
            world.strikeLightning(location.clone().add(Vector.getRandom()));
            int strikes = match.entropyForTick().randomInt(STRIKES);
            for(int i = 0; i < strikes; i++) {
                world.strikeLightningEffect(location.clone().add(Vector.getRandom().multiply(Math.pow(i + 1, 2))));
            }
        });
    }

    @Override
    public int targets() {
        return match.entropyForTick().randomInt(TARGETS);
    }

}
