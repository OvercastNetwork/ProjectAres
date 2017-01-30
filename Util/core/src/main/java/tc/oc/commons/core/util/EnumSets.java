package tc.oc.commons.core.util;

import java.util.Collection;
import java.util.EnumSet;

public final class EnumSets {
    private EnumSets() {}

    /**
     * Create a new {@link EnumSet} of the given type, copying the initial
     * contents from the given set.
     *
     * Unlike {@link EnumSet#copyOf(Collection)}, this method always works,
     * even if the given set is empty, and is not another {@link EnumSet}.
     */
    public static <E extends Enum<E>> EnumSet<E> copySet(Class<E> type, Collection<E> contents) {
        final EnumSet<E> copy = EnumSet.noneOf(type);
        copy.addAll(contents);
        return copy;
    }
}
