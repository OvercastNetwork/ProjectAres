package tc.oc.commons.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import tc.oc.commons.core.ListUtils;

public class C3 {
    private C3() {}

    /**
     * Merge the given lists into a single list using the C3 linearization algorithm
     * https://en.wikipedia.org/wiki/C3_linearization
     *
     * This algorithm is commonly used to flatten multi-parent inheritance trees into
     * a single ancestral line.
     */
    public static <T> List<? extends T> merge(Collection<Collection<? extends T>> parents) {
        final List<List<? extends T>> lists = new ArrayList<>();
        for(Collection<? extends T> parent : parents) {
            if(!parent.isEmpty()) {
                lists.add(new ArrayList<>(parent));
            }
        }

        final ImmutableList.Builder<T> merged = ImmutableList.builder();

        outerLoop: while(!lists.isEmpty()) {
            for(List<? extends T> list : lists) {
                final T head = list.get(0);
                if(!anyTailsContain(lists, head)) {
                    merged.add(head);
                    removeHeads(lists, head);
                    removeEmpty(lists);
                    continue outerLoop;
                }
            }
            throw new IllegalArgumentException("Unable to merge lists due to ordering conflict");
        }

        return merged.build();
    }

    private static <T> boolean anyTailsContain(List<List<? extends T>> lists, T element) {
        for(List<? extends T> list : lists) {
            if(ListUtils.contains(list, element, 1)) return true;
        }
        return false;
    }

    private static <T> void removeHeads(List<List<? extends T>> lists, T element) {
        for(List<? extends T> list : lists) {
            if(Objects.equals(list.get(0), element)) {
                list.remove(0);
            }
        }
    }

    private static <T> void removeEmpty(List<List<? extends T>> lists) {
        for(Iterator<List<? extends T>> iterator = lists.iterator(); iterator.hasNext(); ) {
            if(iterator.next().isEmpty()) iterator.remove();
        }
    }
}
