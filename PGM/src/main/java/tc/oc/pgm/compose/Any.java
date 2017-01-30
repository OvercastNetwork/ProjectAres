package tc.oc.pgm.compose;

import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.random.AdvancingEntropy;
import tc.oc.commons.core.random.Entropy;
import tc.oc.commons.core.random.MutableWeightedRandomChooser;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.Ranges;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.query.ITransientQuery;

public class Any<T> extends CompositionImpl<T> {

    public static class Option<T> extends Inspectable.Impl {
        @Inspect private final double weight;
        @Inspect private final Filter filter;
        @Inspect private final Composition<T> element;

        public Option(double weight, Filter filter, Composition<T> element) {
            this.weight = weight;
            this.filter = filter;
            this.element = element;
        }
    }

    @Inspect private final Range<Integer> count;
    @Inspect private final boolean unique;
    @Inspect private final ImmutableList<Option<T>> options;
    private final double totalWeight;

    public Any(Range<Integer> count, boolean unique, Stream<Option<T>> choices) {
        this(count, unique, choices.collect(Collectors.toImmutableList()));
    }

    public Any(Range<Integer> count, boolean unique, Iterable<Option<T>> choices) {
        this.count = Ranges.toClosed(count);
        this.unique = unique;
        this.options = ImmutableList.copyOf(choices);
        this.totalWeight = this.options.stream().mapToDouble(c -> c.weight).sum();
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public Stream<T> dependencies() {
        return options.stream().flatMap(option -> option.element.dependencies());
    }

    @Override
    public Stream<T> elements(ITransientQuery query) {
        if(totalWeight <= 0) return Stream.empty();

        final MutableWeightedRandomChooser<Option<T>, Double> chooser = new MutableWeightedRandomChooser<>();
        for(Option<T> option : options) {
            if(option.filter.query(query).isAllowed()) {
                chooser.add(option, option.weight);
            }
        }

        final Entropy entropy = new AdvancingEntropy(query.entropy().randomLong());
        Stream<T> result = Stream.empty();
        for(int count = entropy.randomInt(this.count); count > 0 && !chooser.isEmpty(); count--) {
            final Option<T> option = chooser.choose(entropy);
            result = Stream.concat(result, option.element.elements(query));
            if(unique) chooser.remove(option);
        }
        return result;
    }
}
