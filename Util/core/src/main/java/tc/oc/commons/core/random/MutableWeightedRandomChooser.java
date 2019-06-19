package tc.oc.commons.core.random;

import tc.oc.commons.core.stream.Collectors;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A random chooser that supports insertion and removal of choices.
 */
public class MutableWeightedRandomChooser<T, N extends Number> extends WeightedRandomChooser<T, N> {

    private final Map<T, Option> options = new HashMap<>();
    private double totalWeight = 0;

    public MutableWeightedRandomChooser() {}

    public MutableWeightedRandomChooser(Map<T, N> weights) {
        addAll(weights);
    }

    public MutableWeightedRandomChooser(Stream<T> elements, Function<T, N> scale) {
        this(elements.collect(Collectors.mappingTo(scale)));
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
        n *= totalWeight;
        Option result = null;
        for(Option option : options.values()) {
            if(n < option.weight) {
                result = option;
                break;
            } else {
                n -= option.weight;
            }
        }
        return result.element; // NPE should be impossible
    }

    public void add(T choice, N weight) {
        final double doubleWeight = weight.doubleValue();
        if(doubleWeight > 0) {
            totalWeight += doubleWeight;
            options.put(choice, new Option(choice, doubleWeight));
        }
    }

    public void remove(T choice) {
        final Option c = options.remove(choice);
        if(c != null) {
            totalWeight -= c.weight;
        }
    }

    public void addAll(Map<T, N> weightedChoices) {
        weightedChoices.forEach(this::add);
    }

    public void removeAll(Iterable<T> choices) {
        choices.forEach(this::remove);
    }
}
