package tc.oc.commons.core.random;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import tc.oc.commons.core.util.Ranges;

/**
 * Manually iterated random number source that generates 64 random bits per iteration.
 */
public interface Entropy {

    void advance();

    long randomLong();

    default double randomDouble() {
        // Multiply low 53 bits of randomLong() by 1 / 2^53
        return (randomLong() & ((1L << 53) - 1L)) * 0x1.0p-53;
    }

    default int randomInt(Range<Integer> range) {
        final int min = Ranges.needMinimum(range);
        final int delta = Ranges.needOpenMaximum(range) - min;
        return min + (int) ((randomLong() & 0xffffffffL) * delta / 0x100000000L);
    }

    default <T> T randomElement(T... array) {
        return randomElement(Lists.newArrayList(array));
    }

    default <T> T randomElement(Iterable<T> iterable) {
        return Iterables.get(iterable, randomInt(Range.closedOpen(0, Iterables.size(iterable))));
    }

    default <T> T removeRandomElement(List<T> list) {
        return list.remove(randomInt(Range.closedOpen(0, list.size())));
    }
}
