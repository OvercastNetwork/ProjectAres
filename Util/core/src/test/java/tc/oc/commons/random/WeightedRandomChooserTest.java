package tc.oc.commons.random;

import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.MutableEntropy;
import tc.oc.commons.core.random.WeightedRandomChooser;
import tc.oc.commons.core.random.MutableWeightedRandomChooser;
import tc.oc.commons.core.random.ImmutableWeightedRandomChooser;

import static org.junit.Assert.*;
import static tc.oc.test.Assert.*;

public class WeightedRandomChooserTest {

    static final double Δ = 0.00001; // For floating-point comparisons

    static final long SEED = 2905696505089501646L;
    Entropy entropy;

    @Before
    public void setUp() throws Exception {
        entropy = new MutableEntropy(SEED);
    }

    /**
     * Run 10,000 trials and assert the given probability within 1/100
     *
     * This has a small chance of failing, but since we use a fixed random seed,
     * it will do the same thing for every test run. If it starts failing, I guess
     * we just change the SEED.
     */
    <T, N extends Number> void assertProbability(WeightedRandomChooser<T, N> chooser, T choice, double probability) {
        int n = 0;
        for(int i = 0; i < 10000; i++) {
            if(choice.equals(chooser.choose(entropy))) n++;
            entropy.advance();
        }
        assertEquals(probability, n / 10000D, 0.01);
    }

    void assertTotalWeight(double totalWeight, WeightedRandomChooser<?, ?> chooser) {
        assertEquals(totalWeight, chooser.totalWeight(), Δ);
    }

    @Test
    public void empty() throws Exception {
        ImmutableWeightedRandomChooser<?, ?> chooser = new ImmutableWeightedRandomChooser<>(ImmutableMap.of());
        assertTrue(chooser.isEmpty());
        assertTotalWeight(0, chooser);
        assertThrows(NoSuchElementException.class, () -> chooser.choose(entropy));
    }

    @Test
    public void immutable() throws Exception {
        ImmutableWeightedRandomChooser<String, Integer> chooser = new ImmutableWeightedRandomChooser<>(ImmutableMap.of(
            "One", 1,
            "Two", 2,
            "Three", 3
        ));

        assertFalse(chooser.isEmpty());
        assertTotalWeight(6, chooser);
        assertProbability(chooser, "One", 1 / 6D);
        assertProbability(chooser, "Two", 2 / 6D);
        assertProbability(chooser, "Three", 3 / 6D);
    }

    @Test
    public void mutable() throws Exception {
        MutableWeightedRandomChooser<String, Integer> chooser = new MutableWeightedRandomChooser<>();

        chooser.add("One", 1);
        assertTotalWeight(1, chooser);
        assertProbability(chooser, "One", 1 / 1D);

        chooser.add("Two", 2);
        assertTotalWeight(3, chooser);
        assertProbability(chooser, "One", 1 / 3D);
        assertProbability(chooser, "Two", 2 / 3D);

        chooser.add("Three", 3);
        assertTotalWeight(6, chooser);
        assertProbability(chooser, "One", 1 / 6D);
        assertProbability(chooser, "Two", 2 / 6D);
        assertProbability(chooser, "Three", 3 / 6D);

        chooser.remove("One");
        assertTotalWeight(5, chooser);
        assertProbability(chooser, "Two", 2 / 5D);
        assertProbability(chooser, "Three", 3 / 5D);

        chooser.remove("Two");
        assertTotalWeight(3, chooser);
        assertProbability(chooser, "Three", 3 / 3D);

        chooser.remove("Three");
        assertTotalWeight(0, chooser);
        assertThrows(NoSuchElementException.class, () -> chooser.choose(entropy));
    }
}
