package tc.oc.commons.core.util;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import tc.oc.time.Durations;

public class TimeUtils {

    public static final Duration INF_POSITIVE = ChronoUnit.FOREVER.getDuration();
    public static final Duration INF_NEGATIVE = INF_POSITIVE.negated();
    public static final Instant INF_FUTURE = Instant.MAX;
    public static final Instant INF_PAST = Instant.MIN;

    public static boolean isFinite(Duration duration) {
        return !isInfPositive(duration) && !isInfNegative(duration);
    }

    public static boolean isInfPositive(Duration duration) {
        return INF_POSITIVE.equals(duration);
    }

    public static boolean isInfNegative(Duration duration) {
        return INF_NEGATIVE.equals(duration);
    }

    public static boolean isInfFuture(Instant instant) {
        return INF_FUTURE.equals(instant);
    }

    public static boolean isInfPast(Instant instant) {
        return INF_PAST.equals(instant);
    }

    public static boolean isPrecise(TemporalUnit unit) {
        return !unit.isDurationEstimated() || ChronoUnit.DAYS.equals(unit);
    }

    public static long toMicros(Duration duration) {
        return Math.addExact(Math.multiplyExact(duration.getSeconds(), 1_000_000),
                             duration.getNano() / 1_000);
    }

    public static long toUnit(TemporalUnit unit, Duration duration) {
        switch((ChronoUnit) unit) {
            case NANOS:     return duration.toNanos();
            case MICROS:    return toMicros(duration);
            case MILLIS:    return duration.toMillis();
            case SECONDS:   return duration.getSeconds();
        }

        if(unit.getDuration().getNano() == 0) {
            return duration.getSeconds() / unit.getDuration().getSeconds();
        }

        throw new IllegalArgumentException("Unsupported sub-second unit " + unit);
    }

    public static long getUnitOrDefault(TemporalAmount period, TemporalUnit unit, long def) {
        return period.getUnits().contains(unit) ? period.get(unit) : def;
    }

    public static long getUnitOrZero(TemporalAmount period, TemporalUnit unit) {
        return getUnitOrDefault(period, unit, 0L);
    }

    public static Duration duration(Instant start, Instant end) {
        if(isInfPast(start) || isInfFuture(end)) {
            return INF_POSITIVE;
        } else if(start.isBefore(end)) {
            return Duration.between(start, end);
        } else {
            return Duration.ZERO;
        }
    }

    public static Optional<Duration> positiveDuration(Instant start, Instant end) {
        return Optionals.getIf(start.isBefore(end), () -> duration(start, end));
    }

    public static Duration durationUntil(Instant end) {
        return duration(Instant.now(), end);
    }

    public static Duration durationSince(Instant start) {
        return duration(start, Instant.now());
    }

    public static boolean isInfFuture(Date date) {
        return date.getYear() > 8000; // Hacky, but needs to match Ruby's Time::INF_FUTURE
    }

    public static boolean isInfPast(Date date) {
        return date.getYear() < -10000;
    }

    public static Instant toInstant(Date date) {
        if(isInfFuture(date)) {
            return INF_FUTURE;
        } else if(isInfPast(date)) {
            return INF_PAST;
        } else {
            return date.toInstant();
        }
    }

    public static long daysRoundingUp(Duration duration) {
        final long days = duration.toDays();
        return duration.equals(Duration.ofDays(days)) ? days : days + 1;
    }

    public static long toTicks(Duration duration) {
        return duration.toMillis() / 50;
    }

    public static Duration fromTicks(long ticks) {
        return Duration.ofMillis(50 * ticks);
    }

    public static Duration min(Duration a, Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static Duration max(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static Instant min(Instant a, Instant b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static Instant max(Instant a, Instant b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static boolean isEqualOrBeforeNow(Instant now, Instant instant) {
        return !instant.isAfter(now);
    }

    public static boolean isEqualOrBeforeNow(Instant instant) {
        return isEqualOrBeforeNow(Instant.now(), instant);
    }

    public static boolean isEqualOrAfterNow(Instant now, Instant instant) {
        return !instant.isBefore(now);
    }

    public static boolean isEqualOrAfterNow(Instant instant) {
        return isEqualOrAfterNow(Instant.now(), instant);
    }

    public static Duration parseDuration(String text) throws DateTimeParseException {
        if("oo".equals(text)) return INF_POSITIVE;

        // If text looks like a plain number, try to parse it as seconds,
        // but be fairly strict so we don't accidentally parse a time as
        // a number.
        if(text.matches("^\\s*-?[0-9]+(\\.[0-9]+)?\\s*$")) {
            try {
                return Duration.ofMillis((long) (1000 * Double.parseDouble(text)));
            } catch(NumberFormatException ignored) {}
        }

        return Durations.parse(text);
    }

    public static Duration parseDuration(String text, Duration def) throws DateTimeParseException {
        if(text == null || text.length() == 0) {
            return def;
        } else {
            return parseDuration(text);
        }
    }

    public static Instant plus(Instant instant, Duration add) {
        if(isInfFuture(instant)) {
            return INF_FUTURE;
        } else if(isInfPast(instant)) {
            return INF_PAST;
        } else if(isInfPositive(add)) {
            return INF_FUTURE;
        } else if(isInfNegative(add)) {
            return INF_PAST;
        } else {
            return instant.plus(add);
        }
    }

    public static Instant minus(Instant instant, Duration sub) {
        if(isInfFuture(instant)) {
            return INF_FUTURE;
        } else if(isInfPast(instant)) {
            return INF_PAST;
        } else if(isInfPositive(sub)) {
            return INF_PAST;
        } else if(isInfNegative(sub)) {
            return INF_FUTURE;
        } else {
            return instant.minus(sub);
        }
    }

    public static Duration multiply(Duration duration, double factor) {
        final long nanosPerSecond = ChronoUnit.SECONDS.getDuration().toNanos();
        final long nanos = (long) (duration.getNano() * factor);
        return Duration.ofSeconds(Math.addExact((long) (duration.getSeconds() * factor), Math.floorDiv(nanos, nanosPerSecond)),
                                  Math.floorMod(nanos, nanosPerSecond));
    }

    public static Duration ceilSeconds(Duration duration) {
        return duration.getNano() == 0 ? duration : Duration.ofSeconds(duration.getSeconds());
    }

    public static int compareUnits(TemporalUnit a, TemporalUnit b) {
        return a.getDuration().compareTo(b.getDuration());
    }

    private static final Comparator<TemporalUnit> ASC_UNITS = TimeUtils::compareUnits;
    private static final Comparator<TemporalUnit> DESC_UNITS = ASC_UNITS.reversed();

    public static Comparator<TemporalUnit> ascendingUnits() {
        return ASC_UNITS;
    }

    public static Comparator<TemporalUnit> descendingUnits() {
        return DESC_UNITS;
    }
}
