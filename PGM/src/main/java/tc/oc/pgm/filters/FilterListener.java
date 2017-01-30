package tc.oc.pgm.filters;

/**
 * Handler of dynamic {@link Filter} events
 *
 * @see FilterMatchModule#onChange(Class, Filter, FilterListener)
 */
@FunctionalInterface
public interface FilterListener<F extends Filterable<?>> {
    void filterQueryChanged(F filterable, boolean response);
}
