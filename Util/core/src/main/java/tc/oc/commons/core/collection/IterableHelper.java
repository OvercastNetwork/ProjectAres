package tc.oc.commons.core.collection;

import java.util.function.ObjIntConsumer;

/**
 * Can be used as a static helper, or as a mixin.
 */
public interface IterableHelper<E> extends Iterable<E> {

    default boolean containsIdentity(Object object) { return containsIdentity(this, object); }
    static boolean containsIdentity(Iterable<?> iterable, Object object) {
        final int id = System.identityHashCode(object);
        for(Object e : iterable) {
            if(id == System.identityHashCode(e)) {
                return true;
            }
        }
        return false;
    }


    default void forEachIndexed(ObjIntConsumer<E> consumer) { forEachIndexed(this, consumer); }
    static <E> void forEachIndexed(Iterable<E> iterable, ObjIntConsumer<E> consumer) {
        int i = 0;
        for(E e : iterable) {
            consumer.accept(e, i++);
        }
    }

}
