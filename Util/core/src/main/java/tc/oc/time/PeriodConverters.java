package tc.oc.time;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collection;

import tc.oc.commons.core.util.TimeUtils;

public final class PeriodConverters {

    private static final PeriodConverter LARGEST_PRECISE_UNIT = largestPreciseUnit(ChronoUnit.SECONDS);
    private static final PeriodConverter LARGEST_APPROXIMATE_UNIT = largestApproximateUnit(2);
    private static PeriodConverter NORMALIZED = TimePeriod::normalized;
    private static PeriodConverter SECONDS = unit(ChronoUnit.SECONDS);

    private PeriodConverters() {}

    public static PeriodConverter normalized() {
        return NORMALIZED;
    }

    public static PeriodConverter unit(TemporalUnit unit) {
        return duration -> TimePeriod.inUnit(duration, unit);
    }

    public static PeriodConverter units(Collection<TemporalUnit> units) {
        return duration -> TimePeriod.inUnits(duration, units);
    }

    public static PeriodConverter units(TemporalUnit... units) {
        return units(Arrays.asList(units));
    }

    public static PeriodConverter seconds() {
        return SECONDS;
    }

    public static PeriodConverter largestPreciseUnit() {
        return LARGEST_PRECISE_UNIT;
    }

    public static PeriodConverter largestPreciseUnit(TemporalUnit zeroUnit) {
        return duration -> {
            if(duration.isZero()) {
                return TimePeriod.ofUnit(0, zeroUnit);
            }

            for(FriendlyUnits info : FriendlyUnits.descending()) {
                if(duration.minus(TimeUtils.toUnit(info.unit, duration), info.unit).isZero()) {
                    return TimePeriod.inUnit(duration, info.unit);
                }
            }
            throw new IllegalStateException();
        };
    }

    public static PeriodConverter largestApproximateUnit() {
        return LARGEST_APPROXIMATE_UNIT;
    }

    public static PeriodConverter largestApproximateUnit(long minQuantity) {
        return duration -> {
            for(FriendlyUnits info : FriendlyUnits.descending()) {
                if(minQuantity <= TimeUtils.toUnit(info.unit, duration)) {
                    return TimePeriod.inUnit(duration, info.unit);
                }
            }
            return TimePeriod.inUnit(duration, FriendlyUnits.smallest().unit);
        };
    }
}
