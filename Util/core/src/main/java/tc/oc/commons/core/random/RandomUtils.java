package tc.oc.commons.core.random;

import java.util.Random;

import com.google.common.collect.Iterables;
import org.apache.commons.lang.math.Fraction;

public class RandomUtils {

    public static int safeNextInt(Random random, int i) {
        return i <= 0 ? 0 : random.nextInt(i);
    }

    public static <T> T element(Random random, Iterable<? extends T> collection) {
        return Iterables.get(collection, safeNextInt(random, Iterables.size(collection)));
    }

    public static boolean nextBoolean(Random random, Fraction chance) {
        return random.nextDouble() < chance.doubleValue();
    }
}
