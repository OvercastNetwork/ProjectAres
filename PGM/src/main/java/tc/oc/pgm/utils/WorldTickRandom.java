package tc.oc.pgm.utils;

import javax.inject.Inject;

import org.bukkit.World;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.random.Entropy;
import tc.oc.pgm.match.inject.ForMatch;

/**
 * Generates random numbers associated with ticks of the given {@link World}
 */
public class WorldTickRandom {
    private final World world;

    private long tick;
    private final Entropy entropy;

    @Inject public WorldTickRandom(World world, @ForMatch Entropy entropy) {
        this.world = world;
        this.entropy = entropy;
    }

    private void sync() {
        long now = NMSHacks.getMonotonicTime(this.world);
        if(tick != now) {
            tick = now;
            entropy.advance();
        }
    }

    public Entropy entropy() {
        sync();
        return entropy;
    }
}
