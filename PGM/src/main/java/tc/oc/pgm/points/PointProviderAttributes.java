package tc.oc.pgm.points;

import javax.annotation.Nullable;

import tc.oc.commons.core.inspect.Inspectable;

public class PointProviderAttributes extends Inspectable.Impl {
    private final @Inspect @Nullable AngleProvider yawProvider;
    private final @Inspect @Nullable AngleProvider pitchProvider;
    private final @Inspect boolean safe;
    private final @Inspect boolean outdoors;

    public PointProviderAttributes(AngleProvider yawProvider, AngleProvider pitchProvider, boolean safe, boolean outdoors) {
        this.yawProvider = yawProvider;
        this.pitchProvider = pitchProvider;
        this.safe = safe;
        this.outdoors = outdoors;
    }

    public PointProviderAttributes() {
        this(null, null, false, false);
    }

    public boolean hasValues() {
        return yawProvider != null || pitchProvider != null;
    }

    public @Nullable AngleProvider getYawProvider() {
        return yawProvider;
    }

    public @Nullable AngleProvider getPitchProvider() {
        return pitchProvider;
    }

    public boolean isSafe() {
        return safe;
    }

    public boolean isOutdoors() {
        return outdoors;
    }
}
