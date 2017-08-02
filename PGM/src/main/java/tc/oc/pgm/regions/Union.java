package tc.oc.pgm.regions;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;
import tc.oc.commons.core.IterableUtils;

public class Union extends CompoundRegion {
    public Union(Iterable<? extends Region> regions) {
        super(regions);
    }

    public static Region of(Iterable<? extends Region> regions) {
        return IterableUtils.<Region>unify(regions, EmptyRegion.INSTANCE, Union::new);
    }

    public static Region of(Region... regions) {
        return of(Arrays.asList(regions));
    }

    public static Region of(Stream<? extends Region> regions) {
        return of(regions.collect(Collectors.toList()));
    }

    @Override
    public boolean contains(Vector point) {
        return anyMatch(r -> r.contains(point));
    }

    @Override
    public boolean isBlockBounded() {
        return allMatch(Region::isBlockBounded);
    }

    @Override
    public boolean isEmpty() {
        return allMatch(Region::isEmpty);
    }

    @Override
    public boolean isEverywhere() {
        return anyMatch(Region::isEverywhere);
    }

    @Override
    public Cuboid getBounds() {
        return regions.stream()
                      .map(Region::getBounds)
                      .reduce(Cuboid.empty(), Cuboid::union);
    }

    public static Stream<Region> expand(Region region) {
        if(region.isInstanceOf(Union.class)) {
            return region.asInstanceOf(Union.class).regions().stream().flatMap(Union::expand);
        } else {
            return Stream.of(region);
        }
    }
}
