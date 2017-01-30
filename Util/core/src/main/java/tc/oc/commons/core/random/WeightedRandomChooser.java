package tc.oc.commons.core.random;

import java.util.NoSuchElementException;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Holds a set of weighted items which can then looked up
 * with a number in the range [0..1)
 * @param <T> Item type
 * @param <N> Numeric weight type
 */
public abstract class WeightedRandomChooser<T, N extends Number> {

    /**
     * Keep this package-private, don't want it to be part of the interface
     */
    class Option {
        protected final T element;
        protected final double weight;

        protected Option(T element, double weight) {
            this.element = element;
            this.weight = weight;
        }
    }

    public abstract double totalWeight();

    public abstract boolean isEmpty();

    protected abstract T chooseInternal(double n);

    /**
     * Choose an item in a consistent way using the given number.
     * The probability of each item being chosen is proportional
     * its weight. Any particular number will always choose the
     * same item. Beyond that, the choice mechanism is undefined.
     * 
     * @param n Number in the range [0..1)
     * @return An item passed to the constructor, or null if there are no items
     *
     * @throws NoSuchElementException if this chooser is empty
     */
    public T choose(double n) {
        checkArgument(n >= 0);
        checkArgument(n < 1);

        if(isEmpty()) {
            throw new NoSuchElementException("No choices");
        }

        return chooseInternal(n);
    }

    /**
     * Choose an item at random using the given generator.
     * The probability of each item being chosen is proportional
     * its weight. The choice will be consistent with regard to
     * the state of the generator. Beyond that, the choice mechanism
     * is undefined.
     *
     * @param random A Random instance
     * @return An item passed to the constructor
     *
     * @throws NoSuchElementException if this chooser is empty
     */
    public T choose(Random random) {
        return choose(random.nextDouble());
    }

    public T choose(Entropy entropy) {
        return choose(entropy.randomDouble());
    }
}
