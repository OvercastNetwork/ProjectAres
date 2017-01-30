package tc.oc.pgm.regions;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;
import tc.oc.commons.core.IterableUtils;

public class Intersection extends CompoundRegion {

    public Intersection(Iterable<? extends Region> regions) {
        super(regions);
    }

    public Intersection(Region... regions) {
        super(Arrays.asList(regions));
    }

    public static Region of(Iterable<? extends Region> regions) {
        return IterableUtils.unify(regions, EverywhereRegion.INSTANCE, Intersection::new);
    }

    public static Region of(Stream<? extends Region> regions) {
        return of(regions.collect(Collectors.toList()));
    }

    public static Region of(Region... regions) {
        return of(Arrays.asList(regions));
    }

    @Override
    public boolean contains(Vector point) {
        return allMatch(r -> r.contains(point));
    }

    @Override
    public boolean isBlockBounded() {
        return anyMatch(Region::isBlockBounded);
    }

    @Override
    public boolean isEmpty() {
        return anyMatch(Region::isEmpty);
    }

    @Override
    public boolean isEverywhere() {
        return allMatch(Region::isEverywhere);
    }

    @Override
    public Cuboid getBounds() {
        return regions.stream()
                      .map(Region::getBounds)
                      .reduce(Cuboid.unbounded(), Cuboid::intersect);
    }
}
