package tc.oc.pgm.regions;

import java.util.stream.Stream;

import org.bukkit.geometry.Cuboid;
import org.bukkit.util.Vector;

public class Complement extends Region.Impl {
    private final @Inspect Region original;
    private final @Inspect Region subtracted;

    public Complement(Region original, Region subtracted) {
        this.original = original;
        this.subtracted = subtracted;
    }

    @Override
    public Stream<? extends Region> dependencies() {
        return Stream.of(original, subtracted);
    }

    @Override
    public boolean contains(Vector point) {
        return this.original.contains(point) && !this.subtracted.contains(point);
    }

    @Override
    public boolean isBlockBounded() {
        return this.original.isBlockBounded();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty() || subtracted.isEverywhere();
    }

    @Override
    public boolean isEverywhere() {
        return original.isEverywhere() && subtracted.isEmpty();
    }

    @Override
    public Cuboid getBounds() {
        return Cuboid.complement(this.original.getBounds(), this.subtracted.getBounds());
    }
}
