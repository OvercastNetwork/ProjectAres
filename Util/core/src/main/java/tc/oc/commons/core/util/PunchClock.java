package tc.oc.commons.core.util;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tracks the presence and absence of multiple objects over some time span.
 *
 * All times are calculated relative to the time source passed to the constructor,
 * which must provide the current elapsed time at any moment. The elapsed time must
 * be monotonically increasing i.e. it must never decrease, but it can otherwise
 * increase at any rate or not at all.
 */
public class PunchClock<T> {
    // Time reference for all calculations
    private final Supplier<Duration> timeSource;

    // Most recent punch in/out time
    private final Map<T, Duration> inTimes = new HashMap<>();
    private final Map<T, Duration> outTimes = new HashMap<>();

    // Cumulative punched in/out time, EXCLUDING any current interval
    private final Map<T, Duration> cumulativePresence = new DefaultMapAdapter<>(Duration.ZERO);

    // Maximum continuous punched out interval, EXCLUDING any current one
    private final Map<T, Duration> maxContinuousAbsence = new DefaultMapAdapter<>(Duration.ZERO);

    // Time of first punch-in
    private final Map<T, Duration> firstPresence = new HashMap<>();

    public PunchClock(Supplier<Duration> timeSource) {
        this.timeSource = checkNotNull(timeSource);
    }
    
    public Duration getElapsed() {
        return timeSource.get();
    }
    
    /**
     * Time since the given key last punched in, or zero if the key has never punched in
     */
    public Duration getContinuousPresence(T key) {
        Duration inTime = inTimes.get(key);
        if(inTime == null) {
            return Duration.ZERO;
        } else {
            return getElapsed().minus(inTime);
        }
    }

    /**
     * Time since the given key last punched out, zero if they are currently punched in,
     * current time if they have never punched in or out
     */
    public Duration getContinuousAbsence(T key) {
        Duration outTime = outTimes.get(key);
        if(outTime != null) {
            return getElapsed().minus(outTime);
        } else if(inTimes.containsKey(key)) {
            return Duration.ZERO;
        } else {
            return getElapsed();
        }
    }

    public Duration getMaxContinuousAbsence(T key) {
        return TimeUtils.max(maxContinuousAbsence.get(key), getContinuousAbsence(key));
    }

    /**
     * Total time that the given key has ever been punched in
     */
    public Duration getCumulativePresence(T key) {
        return cumulativePresence.get(key).plus(getContinuousPresence(key));
    }

    public double getCumulativePresencePercent(T key) {
        return (double) getCumulativePresence(key).toMillis() / getElapsed().toMillis();
    }

    /**
     * Total time that the given key has ever not been punched in
     */
    public Duration getCumulativeAbsence(T key) {
        return getElapsed().minus(getCumulativePresence(key));
    }

    public double getCumulativeAbsencePercent(T key) {
        return (double) getCumulativeAbsence(key).toMillis() / getElapsed().toMillis();
    }

    public @Nullable Duration getFirstPresence(T key) {
        Duration t = firstPresence.get(key);
        if(t != null && Comparables.lessThan(t, getElapsed())) {
            return t;
        } else {
            return null;
        }
    }

    public boolean isPresent(T key) {
        return inTimes.containsKey(key);
    }

    public boolean isAbsent(T key) {
        return !isPresent(key);
    }

    public Set<T> getAllPresent() {
        return inTimes.keySet();
    }

    public Set<T> getAllWithPresence() {
        return Sets.union(cumulativePresence.keySet(), inTimes.keySet());
    }

    public Set<T> getAllWithNonZeroPresence() {
        final Duration elapsed = getElapsed();
        if(Comparables.greaterThan(elapsed, Duration.ZERO)) {
            return Sets.union(
                cumulativePresence.keySet(),
                Sets.filter(
                    inTimes.keySet(),
                    new Predicate<T>() {
                        @Override public boolean apply(@Nullable T key) {
                            return Comparables.greaterThan(getElapsed(), inTimes.get(key));
                        }
                    }
                )
            );
        } else {
            return Collections.emptySet();
        }
    }

    private void setOrClear(Map<T, Duration> map, T key, Duration time) {
        if(Comparables.greaterThan(time, Duration.ZERO)) {
            map.put(key, time);
        } else {
            map.remove(key);
        }
    }

    public void punchIn(T key) {
        if(!isPresent(key)) {
            setOrClear(maxContinuousAbsence, key, getMaxContinuousAbsence(key));
            outTimes.remove(key);
            inTimes.put(key, getElapsed());
            if(!firstPresence.containsKey(key)) firstPresence.put(key, getElapsed());
        }
    }

    public void punchOut(T key) {
        if(isPresent(key)) {
            setOrClear(cumulativePresence, key, getCumulativePresence(key));
            inTimes.remove(key);
            outTimes.put(key, getElapsed());
        }
    }
}
