package tc.oc.commons.core.collection;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.collect.ForwardingMap;
import java.time.Duration;
import java.time.Instant;
import tc.oc.time.Clock;
import tc.oc.commons.core.util.Comparables;

/**
 * A general purpose map of {@link K} -> {@link Instant} that knows
 * the current time through a provided {@link Clock}, and provides
 * some extra time-related methods.
 */
public class InstantMap<K> extends ForwardingMap<K, Instant> implements MapHelper<K, Instant> {

    private final @Nullable Clock clock;
    private final Map<K, Instant> map;

    public InstantMap(Clock clock, Map<K, Instant> map) {
        this.clock = clock;
        this.map = map;
    }

    public InstantMap(Clock clock) {
        this(clock, new HashMap<>());
    }

    @Override
    protected Map<K, Instant> delegate() {
        return map;
    }

    /**
     * Set the given key to the current time, and return the previous time, if any.
     */
    public @Nullable Instant put(K key) {
        return put(key, clock.instant());
    }

    /**
     * Set key to given time unless current value is after given time
     *
     * @return null if key was changed, otherwise current value for key
     */
    public @Nullable Instant putUnlessAfter(K key, Instant time) {
        final Instant then = get(key);
        if(then == null || !then.isAfter(time)) {
            put(key, time);
            return null;
        }
        return then;
    }

    /**
     * Set key to now unless current value is newer than given age
     *
     * @return null if key was changed, otherwise current value for key
     */
    public @Nullable Instant putUnlessNewer(K key, Duration age) {
        final Instant then = get(key), now = clock.instant();
        // Don't do arithmetic on age, because it may overflow
        if(then == null || Comparables.greaterOrEqual(Duration.between(then, now), age)) {
            put(key, now);
            return null;
        }
        return then;
    }
}
