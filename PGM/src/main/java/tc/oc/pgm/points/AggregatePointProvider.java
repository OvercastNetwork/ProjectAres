package tc.oc.pgm.points;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.util.Lazy;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.Union;

public abstract class AggregatePointProvider extends Inspectable.Impl implements PointProvider {

    protected final @Inspect List<PointProvider> children;
    private final @Inspect Lazy<Region> region;

    public AggregatePointProvider(Collection<? extends PointProvider> children) {
        this.children = ImmutableList.copyOf(children);
        region = Lazy.from(() -> Union.of(children.stream().map(PointProvider::getRegion)));
    }

    @Override
    public int getWeight() {
        return children.stream().mapToInt(PointProvider::getWeight).sum();
    }

    @Override
    public Region getRegion() {
        return region.get();
    }

    protected boolean allChildrenCanFail() {
        return children.stream().allMatch(PointProvider::canFail);
    }

    protected boolean anyChildrenCanFail() {
        return children.stream().anyMatch(PointProvider::canFail);
    }
}
