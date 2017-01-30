package tc.oc.commons.random;

import org.junit.Test;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.MutableEntropy;
import tc.oc.commons.core.random.SaltedEntropy;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class SaltedEntropyTest {

    @Test
    public void saltedDifferentFromUnsalted() throws Exception {
        Entropy unsalted = new MutableEntropy();
        Entropy salted = new SaltedEntropy(unsalted, hashCode());
        assertNotEquals(unsalted.randomLong(), salted.randomLong());
    }

    @Test
    public void saltedDifferentFromEachOther() throws Exception {
        Entropy unsalted = new MutableEntropy();
        Entropy salted1 = new SaltedEntropy(unsalted, hashCode());
        Entropy salted2 = new SaltedEntropy(unsalted, ~hashCode());
        assertNotEquals(salted1.randomLong(), salted2.randomLong());
    }

    @Test
    public void saltedAdvancesWithUnsalted() throws Exception {
        Entropy unsalted = new MutableEntropy();
        Entropy salted = new SaltedEntropy(unsalted, hashCode());

        long n = salted.randomLong();
        assertEquals(n, salted.randomLong());

        unsalted.advance();
        assertNotEquals(n, salted.randomLong());

        n = salted.randomLong();
        assertEquals(n, salted.randomLong());

        salted.advance();
        assertNotEquals(n, salted.randomLong());
    }
}
