package tc.oc.pgm.points;

import java.util.Collection;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import tc.oc.commons.core.util.Lazy;
import tc.oc.pgm.match.Match;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;

/**
 * Try a random child up to 100 times
 */
public class RandomPointProvider extends AggregatePointProvider {

    private static final int MAX_ATTEMPTS = 100;

    private final Lazy<ImmutableWeightedRandomChooser<PointProvider, Integer>> chooser = Lazy.from(
        () -> new ImmutableWeightedRandomChooser<>(children.stream(), PointProvider::getWeight)
    );

    public RandomPointProvider(Collection<? extends PointProvider> children) {
        super(children);
    }

    @Override
    public Location getPoint(Match match, @Nullable Entity entity) {
        if(children.isEmpty()) return null;

        for(int i = 0; i < MAX_ATTEMPTS; i++) {
            Location location = chooser.get().choose(match.getRandom()).getPoint(match, entity);
            if(location != null) return location;
        }

        return null;
    }

    @Override
    public boolean canFail() {
        return anyChildrenCanFail();
    }
}
