package tc.oc.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import tc.oc.commons.core.IterableUtils;
import tc.oc.commons.core.util.Comparables;
import tc.oc.commons.core.util.TimeUtils;

public class TimePeriod implements TemporalAmount {

    private static final ImmutableList<TemporalUnit> NORMAL_UNITS = ImmutableList.of(
        ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS,
        ChronoUnit.MILLIS, ChronoUnit.MICROS, ChronoUnit.NANOS
    );

    private final ImmutableMap<TemporalUnit, Long> values;
    private final ImmutableList<TemporalUnit> units;

    private TimePeriod(ImmutableMap<TemporalUnit, Long> values) {
        this.values = values;
        this.units = ImmutableList.copyOf(values.keySet());
    }

    public static TimePeriod ofUnits(Map<TemporalUnit, Long> values) {
        final ImmutableMap.Builder<TemporalUnit, Long> builder = ImmutableMap.builder();
        values.keySet().stream().sorted(Collections.reverseOrder())
              .forEach(unit -> builder.put(unit, values.get(unit)));
        return new TimePeriod(builder.build());
    }

    public static TimePeriod ofUnit(long amount, TemporalUnit unit) {
        return new TimePeriod(ImmutableMap.of(unit, amount));
    }

    public static TimePeriod inUnits(Duration duration, Collection<TemporalUnit> units) {
        final ImmutableMap.Builder<TemporalUnit, Long> builder = ImmutableMap.builder();
        for(TemporalUnit unit : IterableUtils.sorted(units, TimeUtils.descendingUnits())) {
            if(!TimeUtils.isPrecise(unit)) {
                throw new IllegalArgumentException("Imprecise unit " + unit);
            }

            if(Comparables.greaterOrEqual(duration, unit.getDuration())) {
                final long amount = TimeUtils.toUnit(unit, duration);
                builder.put(unit, amount);
                duration = duration.minus(amount, unit);
            }
        }
        return new TimePeriod(builder.build());
    }

    public static TimePeriod inUnits(Duration duration, TemporalUnit... units) {
        return inUnits(duration, Arrays.asList(units));
    }

    public static TimePeriod inUnit(Duration duration, TemporalUnit unit) {
        return ofUnit(TimeUtils.toUnit(unit, duration), unit);
    }

    public static TimePeriod normalized(Duration duration) {
        return inUnits(duration, NORMAL_UNITS);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return units;
    }

    @Override
    public long get(TemporalUnit unit) {
        return values.getOrDefault(unit, 0L);
    }

    public Duration getDuration() {
        return values.entrySet()
                     .stream()
                     .reduce(Duration.ZERO, (d, e) -> d.plus(e.getValue(), e.getKey()), Duration::plus);
    }

    public TimePeriod normalized() {
        return normalized(getDuration());
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        for(Map.Entry<TemporalUnit, Long> entry : values.entrySet()) {
            temporal = temporal.plus(entry.getValue(), entry.getKey());
        }
        return temporal;
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        for(Map.Entry<TemporalUnit, Long> entry : values.entrySet()) {
            temporal = temporal.minus(entry.getValue(), entry.getKey());
        }
        return temporal;
    }
}
