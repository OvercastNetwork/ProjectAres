package tc.oc.debug;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

import tc.oc.commons.core.util.Pair;

/**
 * Implements {@link #hashCode()} based on a serial number which is specific to the current
 * thread name and a type (defaulting to the this object's class).
 *
 * An optional salt string can be given through the system property:
 *
 *     tc.oc.debug.DeterministicHashcode.salt
 *
 * This should generate more deterministic hashCodes between JVM instances, compared to the
 * default implementation which is based on memory addresses and is highly unpredictable.
 *
 * This can be used to find heisenbugs, but should not be used in production.
 */
public class DeterministicHashcode {

    private static String salt;
    private static String salt() {
        if(salt == null) {
            salt = System.getProperty(DeterministicHashcode.class.getName() + ".salt", "");
            System.out.println(DeterministicHashcode.class.getSimpleName() + " using salt '" + salt + "'");
        }
        return salt;
    }

    private static final ConcurrentMap<Object, Integer> ORDINALS = new ConcurrentHashMap<>();

    private final int hashCode;

    public DeterministicHashcode() { this(null); }
    public DeterministicHashcode(@Nullable Class<?> type) {
        final Object key = Pair.of(Thread.currentThread().getName(),
                                   type != null ? type : getClass());
        final Integer ordinal = ORDINALS.compute(key, (cls, count) -> count == null ? 0 : count + 1);
        hashCode = Objects.hash(salt(), getClass().getName(), ordinal);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
