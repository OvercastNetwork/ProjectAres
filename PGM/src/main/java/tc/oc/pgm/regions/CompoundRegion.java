package tc.oc.pgm.regions;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import tc.oc.commons.core.stream.Collectors;

public abstract class CompoundRegion extends Region.Impl {

    @Inspect(brief = true)
    protected final ImmutableList<? extends Region> regions;

    protected CompoundRegion(Iterable<? extends Region> regions) {
        this.regions = ImmutableList.copyOf(regions);
    }

    protected CompoundRegion(Stream<? extends Region> regions) {
        this.regions = regions.collect(Collectors.toImmutableList());
    }

    public List<? extends Region> regions() {
        return regions;
    }

    protected boolean allMatch(Predicate<? super Region> predicate) {
        return regions.stream().allMatch(predicate);
    }

    protected boolean anyMatch(Predicate<? super Region> predicate) {
        return regions.stream().anyMatch(predicate);
    }

    @Override
    public Stream<? extends Region> dependencies() {
        return regions.stream();
    }
}
