package tc.oc.commons.random;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
import org.junit.Test;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.MutableEntropy;
import tc.oc.commons.core.util.Ranges;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;


public class EntropyTest {

    // Make the tests repeatable
    private static final long SEED = 4512060816665834742L;

    @Test
    public void doubleRange() throws Exception {
        Entropy e = new MutableEntropy(SEED);
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;

        // Choose 10k values and track the min and max
        for(int i = 0; i < 10000; i++) {
            final double n = e.randomDouble();
            min = Math.min(min, n);
            max = Math.max(max, n);
            e.advance();
        }

        // Assert that no value was outside the valid range
        assertTrue(min >= 0);
        assertTrue(max < 1);

        // Assert that the min and max values pretty close to 0 and 1
        assertEquals(0, min, 0.01);
        assertEquals(1, max, 0.01);
    }

    @Test
    public void intRange() throws Exception {
        Entropy e = new MutableEntropy(SEED);
        Range<Integer> range = Range.closedOpen(-5, 5);
        Multiset<Integer> distribution = HashMultiset.create();

        // Choose 1k values and check that they are in the range
        for(int i = 0; i < 10000; i++) {
            final int value = e.randomInt(range);
            assertContains(range, value);
            distribution.add(value);
            e.advance();
        }

        // Assert that each of the 10 values was chosen ~1000 times
        Ranges.forEach(range, value -> {
            assertEquals(1000D, distribution.count(value), 50D);
        });
    }

    @Test
    public void advance() throws Exception {
        Entropy e = new MutableEntropy();
        final double d = e.randomDouble();
        assertEquals(d, e.randomDouble(), 0);
        e.advance();
        assertNotEquals(d, e.randomDouble(), 0);
    }
}
