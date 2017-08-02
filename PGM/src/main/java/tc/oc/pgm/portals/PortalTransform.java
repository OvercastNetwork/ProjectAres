package tc.oc.pgm.portals;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.EntityLocation;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import tc.oc.pgm.PGM;
import tc.oc.pgm.regions.Region;

import static com.google.common.base.Preconditions.checkNotNull;

public interface PortalTransform extends InvertibleOperator<PortalTransform> {

    Vector apply(Vector v);

    Location apply(Location v);

    default EntityLocation apply(EntityLocation v) {
        return (EntityLocation) apply((Location) v);
    }

    static PortalTransform piecewise(DoubleTransform x, DoubleTransform y, DoubleTransform z, DoubleTransform yaw, DoubleTransform pitch) {
        if(x instanceof DoubleTransform.Identity &&
           y instanceof DoubleTransform.Identity &&
           z instanceof DoubleTransform.Identity &&
           yaw instanceof DoubleTransform.Identity &&
           pitch instanceof DoubleTransform.Identity) {
            return IDENTITY;
        } else {
            return new Piecewise(x, y, z, yaw, pitch);
        }
    }

    class Piecewise implements PortalTransform {
        private final DoubleTransform x, y, z, yaw, pitch;

        private Piecewise(DoubleTransform x, DoubleTransform y, DoubleTransform z, DoubleTransform yaw, DoubleTransform pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        private Vector mutate(Vector v) {
            v.setX(x.applyAsDouble(v.getX()));
            v.setY(y.applyAsDouble(v.getY()));
            v.setZ(z.applyAsDouble(v.getZ()));
            return v;
        }

        private Location mutate(Location v) {
            mutate(v.position());
            v.setYaw((float) yaw.applyAsDouble(v.getYaw()));
            v.setPitch((float) pitch.applyAsDouble(v.getPitch()));
            return v;
        }

        @Override
        public Vector apply(Vector v) {
            return mutate(new Vector(v));
        }

        @Override
        public Location apply(Location v) {
            return mutate(v.clone());
        }

        @Override
        public boolean invertible() {
            return x.invertible() &&
                   y.invertible() &&
                   z.invertible() &&
                   yaw.invertible() &&
                   pitch.invertible();
        }

        @Override
        public PortalTransform inverse() {
            return new Piecewise(x.inverse(),
                                 y.inverse(),
                                 z.inverse(),
                                 yaw.inverse(),
                                 pitch.inverse());
        }
    }

    Identity IDENTITY = new Identity();
    class Identity implements PortalTransform {
        private Identity() {}

        @Override
        public Location apply(Location v) {
            return v;
        }

        @Override
        public Vector apply(Vector v) {
            return v;
        }

        @Override
        public boolean invertible() {
            return true;
        }

        @Override
        public PortalTransform inverse() {
            return this;
        }
    }

    static PortalTransform regional(Optional<Region> from, Region to) {
        return new Regional(from, to);
    }

    class Regional implements PortalTransform {
        private final Optional<Region> from;
        private final Region to;

        private Regional(Optional<Region> from, Region to) {
            this.from = checkNotNull(from);
            this.to = checkNotNull(to);
        }

        @Override
        public Vector apply(Vector v) {
            v = new Vector(v);
            v.copy(to.getRandom(PGM.getMatchManager().needCurrentMatch().getRandom()));
            return v;
        }

        @Override
        public Location apply(Location v) {
            v = v.clone();
            v.setPosition(to.getRandom(PGM.getMatchManager().needMatch(v.getWorld()).getRandom()));
            return v;
        }

        @Override
        public boolean invertible() {
            return from != null;
        }

        @Override
        public PortalTransform inverse() {
            from.orElseThrow(() -> new IllegalStateException("not invertible"));
            return new Regional(Optional.of(to), from.get());
        }
    }

    static PortalTransform concatenate(PortalTransform first, PortalTransform last) {
        if(first instanceof Identity) {
            return last;
        } else if (last instanceof Identity) {
            return first;
        } else {
            return new Concatenate(first, last);
        }
    }

    class Concatenate implements PortalTransform {
        private final PortalTransform first, last;

        private Concatenate(PortalTransform first, PortalTransform last) {
            this.first = checkNotNull(first);
            this.last = checkNotNull(last);
        }

        @Override
        public Vector apply(Vector v) {
            return last.apply(first.apply(v));
        }

        @Override
        public Location apply(Location v) {
            return last.apply(first.apply(v));
        }

        @Override
        public EntityLocation apply(EntityLocation v) {
            return last.apply(first.apply(v));
        }

        @Override
        public boolean invertible() {
            return first.invertible() && last.invertible();
        }

        @Override
        public PortalTransform inverse() {
            return new Concatenate(last, first);
        }
    }
}

