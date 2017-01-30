package tc.oc.commons.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import tc.oc.commons.core.reflect.Types;

public class Ranges {
    private Ranges() {}

    public static void assertLowerBound(Range<?> range) {
        if(!range.hasLowerBound()) {
            throw new IllegalArgumentException("Range has no lower bound");
        }
    }

    public static void assertUpperBound(Range<?> range) {
        if(!range.hasLowerBound()) {
            throw new IllegalArgumentException("Range has no upper bound");
        }
    }

    public static void assertBounded(Range<?> range) {
        assertLowerBound(range);
        assertUpperBound(range);
    }

    public static Optional<Integer> minimum(Range<Integer> range) {
        return range.hasLowerBound() ? Optional.of(range.lowerBoundType() == BoundType.CLOSED ? range.lowerEndpoint()
                                                                                              : Integer.valueOf(range.lowerEndpoint() + 1))
                                     : Optional.empty();
    }

    public static Optional<Integer> maximum(Range<Integer> range) {
        return range.hasUpperBound() ? Optional.of(range.upperBoundType() == BoundType.CLOSED ? range.upperEndpoint()
                                                                                              : Integer.valueOf(range.upperEndpoint() - 1))
                                     : Optional.empty();
    }

    public static int needMinimum(Range<Integer> range) {
        assertLowerBound(range);
        return range.lowerBoundType() == BoundType.CLOSED ? range.lowerEndpoint()
                                                          : range.lowerEndpoint() + 1;
    }

    public static int needOpenMinimum(Range<Integer> range) {
        assertLowerBound(range);
        return range.lowerBoundType() == BoundType.CLOSED ? range.lowerEndpoint() - 1
                                                          : range.lowerEndpoint();
    }

    public static int needMaximum(Range<Integer> range) {
        assertUpperBound(range);
        return range.upperBoundType() == BoundType.CLOSED ? range.upperEndpoint()
                                                          : range.upperEndpoint() - 1;
    }

    public static int needOpenMaximum(Range<Integer> range) {
        assertUpperBound(range);
        return range.upperBoundType() == BoundType.CLOSED ? range.upperEndpoint() + 1
                                                          : range.upperEndpoint();
    }

    public static Range<Integer> toClosed(Range<Integer> range) {
        assertBounded(range);
        return range.lowerBoundType() == BoundType.CLOSED && range.upperBoundType() == BoundType.CLOSED
               ? range
               : Range.closed(needMinimum(range), needMaximum(range));
    }

    public static void forEach(Range<Integer> range, IntConsumer consumer) {
        final int max = needOpenMaximum(range);
        for(int i = needMinimum(range); i < max; i++) {
            consumer.accept(i);
        }
    }

    public static <T extends Comparable<T>> TypeToken<Range<T>> typeOf(TypeToken<T> type) {
        return new TypeToken<Range<T>>(){}.where(new TypeParameter<T>(){}, type);
    }

    public static <T extends Comparable<T>> TypeLiteral<Range<T>> typeOf(TypeLiteral<T> type) {
        return Types.toLiteral(typeOf(Types.toToken(type)));
    }

    /**
     * Return an english phrase describing the given {@link Range} e.g.
     *
     *     Range.all()                      -> "unbounded"
     *     Range.singleton(3)               -> "3"
     *     Range.atLeast(3)                 -> "at least 3"
     *     Range.closedOpen(3, 7)           -> "at least 3 and less than 7"
     *     Range.closed(3, 7)               -> "between 3 and 7"
     */
    public static String describe(Range<?> range) {
        if(range.hasLowerBound() && range.hasUpperBound() && range.lowerBoundType() == BoundType.CLOSED && range.upperBoundType() == BoundType.CLOSED) {
            if(range.lowerEndpoint().equals(range.upperEndpoint())) {
                // singleton
                return range.lowerEndpoint().toString();
            } else {
                // closed-closed
                return "between " + range.lowerEndpoint() + " and " + range.upperEndpoint();
            }
        }

        final List<String> parts = new ArrayList<>(2);

        if(range.hasLowerBound()) {
            parts.add((range.lowerBoundType() == BoundType.CLOSED ? "at least " : "more than ") + range.lowerEndpoint());
        }

        if(range.hasUpperBound()) {
            parts.add((range.upperBoundType() == BoundType.CLOSED ? "at most " : "less than ") + range.upperEndpoint());
        }

        switch(parts.size()) {
            case 0: return "unbounded";
            case 1: return parts.get(0);
            default: return parts.get(0) + " and " + parts.get(1);
        }
    }
}
