package tc.oc.commons.core.util;

import org.apache.commons.lang.mutable.MutableInt;
import tc.oc.commons.core.random.RandomUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A map that can ensure even distribution of key outputs.
 */
public class UsageCollection<E> extends HashMap<E, UsageCollection.Index> {

    private final Random random;

    public UsageCollection(Random random, Collection<E> initial) {
        super();
        this.random = random;
        initial.forEach(this::use);
    }

    /**
     * Add or use an object to this usage collection.
     * @param object the object to add or use.
     */
    public void use(E object) {
        if(!containsKey(object)) {
            put(object, new Index());
        } else {
            get(object).change(+1);
        }
    }

    /**
     * Allocate an additional usage space for an object.
     * @param object the object to allocate an additional space for.
     */
    public void allocate(E object) {
        if(containsValue(object)) {
            get(object).change(-1);
        }
    }

    /**
     * Get the object with the least amount of usages.
     * @return the least used object.
     */
    public @Nullable E next() {
        if(isEmpty()) {
            return null;
        } else {
            E next = getLeastKey();
            use(next);
            return next;
        }
    }

    protected @Nullable E getLeastKey() {
        List<E> leastKeys = new ArrayList<>();
        int leastValue = Integer.MAX_VALUE;
        for(Map.Entry<E, Index> entry : entrySet()) {
            int value = entry.getValue().intValue();
            if(entry.getValue().intValue() < leastValue) {
                leastValue = value;
                leastKeys.clear();
            }
            if(value == leastValue) {
                leastKeys.add(entry.getKey());
            }
        }
        return leastKeys.isEmpty() ? null : RandomUtils.element(random, leastKeys);
    }

    static class Index extends MutableInt {

        public Index() {
            super(0);
        }

        public Index change(int delta) {
            setValue(Math.max(0, intValue() + delta));
            return this;
        }

    }

}
