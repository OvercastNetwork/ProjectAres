package tc.oc.commons.core.random;

import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSortedMap;
import tc.oc.commons.core.stream.Collectors;

/**
 * An immutable chooser.
 *
 * Choices and weights are provided at creation time and never change.
 */
public class ImmutableWeightedRandomChooser<T, N extends Number> extends WeightedRandomChooser<T, N> {

    /**
     * Choices indexed by their cumulative weight i.e. the sum of
     * their own weight and the weights of all choices below them.
     *
     * The last key in this map must always be equal to {@link #totalWeight()}.
     * If the map is empty, then {@link #totalWeight()} must be 0.
     */
    private final NavigableMap<Double, Option> options;

    private final double totalWeight;

    public ImmutableWeightedRandomChooser(Stream<T> elements, Function<T, N> scale) {
        this(elements.collect(Collectors.mappingTo(scale)));
    }

    /**
     * Construct a chooser with the given items
     * @param weights Map of items to weights
     */
    public ImmutableWeightedRandomChooser(Map<T, N> weights) {
        final ImmutableSortedMap.Builder<Double, Option> builder = ImmutableSortedMap.naturalOrder();
        double total = 0;
        for(Map.Entry<T, N> entry : weights.entrySet()) {
            double weight = entry.getValue().doubleValue();
            if(weight > 0) {
                total += weight;
                builder.put(total, new Option(entry.getKey(), weight));
            }
        }
        this.options = builder.build();
        this.totalWeight = total;
    }

    @Override
    public boolean isEmpty() {
        return options.isEmpty();
    }

    @Override
    public double totalWeight() {
        return totalWeight;
    }

    @Override
    protected T chooseInternal(double n) {
        final double key = n * totalWeight;

        // Does random < 1 always imply key < totalWeight?
        // Not sure, due to rounding
        return (key < totalWeight ? options.higherEntry(key)
                                  : options.lastEntry())
            .getValue().element;
    }
}
