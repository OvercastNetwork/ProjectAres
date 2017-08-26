package tc.oc.pgm.animation;

import org.bukkit.World;
import tc.oc.pgm.features.Feature;

import java.time.Duration;
import java.util.List;

public interface Animation extends Feature<AnimationDefinition> {
    void place(Frame frame);

    World getWorld();

    Duration getAfter();

    Duration getLoop();

    int getCount();

    List<Frame> getFrames();
}
