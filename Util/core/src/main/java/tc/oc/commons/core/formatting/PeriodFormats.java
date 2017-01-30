package tc.oc.commons.core.formatting;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collection;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.time.FriendlyUnits;
import tc.oc.time.PeriodConverters;
import tc.oc.time.PeriodRenderers;

import static com.google.common.base.Preconditions.checkArgument;

public class PeriodFormats {

    public static BaseComponent formatColons(Duration duration, Collection<TemporalUnit> units) {
        return PeriodRenderers.colons().renderPeriod(PeriodConverters.units(units).toPeriod(duration));
    }

    public static BaseComponent formatColons(Duration duration, TemporalUnit... units) {
        return formatColons(duration, Arrays.asList(units));
    }

    public static BaseComponent formatColons(Duration duration) {
        return formatColons(duration, ChronoUnit.MINUTES, ChronoUnit.SECONDS);
    }

    public static BaseComponent formatColonsLong(Duration duration) {
        return formatColons(duration, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS);
    }

    public static BaseComponent formatColonsPrecise(Duration duration) {
        return formatColons(duration, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS);
    }

    /**
     * Return a localized description of the given time period, which must contain exactly one field.
     */
    public static BaseComponent formatPeriod(long amount, TemporalUnit unit) {
        return new TranslatableComponent(FriendlyUnits.get(unit).key(amount), String.valueOf(amount));
    }

    public static BaseComponent formatPeriod(TemporalAmount period) {
        checkArgument(period.getUnits().size() == 1);
        final TemporalUnit unit = period.getUnits().get(0);
        return formatPeriod(period.get(unit), unit);
    }

    /**
     * A (localized) interval phrase like "X seconds" or "X minutes". Uses the largest unit
     * that can represent the interval precisely. This is useful when the interval is
     * expected to be a "round" value. If duration is zero, the smallest unit is used.
     */
    public static BaseComponent briefNaturalPrecise(Duration duration) {
        return formatPeriod(PeriodConverters.largestPreciseUnit().toPeriod(duration));
    }

    /**
     * A (localized) interval phrase like "X seconds" or "X minutes". Uses the largest unit that
     * fits into the interval at least the given number of times. If there is no such unit,
     * the smallest unit available is used.
     */
    public static BaseComponent briefNaturalApproximate(Duration duration, long minQuantity) {
        return formatPeriod(PeriodConverters.largestApproximateUnit(minQuantity).toPeriod(duration));
    }

    public static BaseComponent briefNaturalApproximate(Instant begin, Instant end, long minQuantity) {
        return briefNaturalApproximate(Duration.between(begin, end), minQuantity);
    }

    /**
     * A (localized) interval phrase like "X seconds" or "X minutes". Uses the largest unit
     * that fits into the interval at least twice. The interval must be non-zero.
     */
    public static BaseComponent briefNaturalApproximate(Duration duration) {
        return briefNaturalApproximate(duration, 2);
    }

    public static BaseComponent briefNaturalApproximate(Instant begin, Instant end) {
        return briefNaturalApproximate(begin, end, 2);
    }

    public static BaseComponent relativePastApproximate(Instant then) {
        return new TranslatableComponent("time.ago", briefNaturalApproximate(Duration.between(then, Instant.now())));
    }

    public static BaseComponent relativeFutureApproximate(Instant then, Instant future) {
        return new TranslatableComponent("time.for", briefNaturalApproximate(Duration.between(then, future)));
    }

    public static BaseComponent relativeFutureApproximate(Instant future) {
        return new TranslatableComponent("time.fromNow", briefNaturalApproximate(Duration.between(Instant.now(), future)));
    }
}
