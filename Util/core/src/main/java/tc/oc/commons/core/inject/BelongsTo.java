package tc.oc.commons.core.inject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import javax.inject.Qualifier;

import com.google.common.collect.Iterators;

/**
 * Associates a class with another class, for whatever purpose.
 *
 * When used as a binding annotation for @Inject, it qualifies the resolved dependency
 * as the one belonging to the given class. This could be established through a chain
 * of @BelongsTo annotations, or through some other means.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface BelongsTo {
    Class<?> value();

    class Impl implements BelongsTo {
        private final Class<?> v;

        public Impl(Class<?> v) {
            this.v = v;
        }

        @Override
        public Class<?> value() {
            return v;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return BelongsTo.class;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BelongsTo &&
                   value().equals(((BelongsTo) obj).value());
        }

        @Override
        public int hashCode() {
            return value().hashCode();
        }

        @Override
        public String toString() {
            return "@" + BelongsTo.class.getSimpleName() + "(" + value().getSimpleName() + ")";
        }

        public static Iterator<Class<?>> ownerIterator(AnnotatedElement element) {
            final BelongsTo belongsTo = element.getAnnotation(BelongsTo.class);
            return new Iterator<Class<?>>() {
                final Set<Class<?>> seen = new HashSet<>();
                Class<?> cls = belongsTo == null ? null : belongsTo.value();

                @Override
                public boolean hasNext() {
                    return cls != null;
                }

                @Override
                public Class<?> next() {
                    if(cls == null) throw new NoSuchElementException();
                    final Class<?> next = cls;
                    final BelongsTo belongsTo = cls.getAnnotation(BelongsTo.class);
                    cls = belongsTo != null && seen.add(belongsTo.value()) ? belongsTo.value() : null;
                    return next;
                }
            };
        }

        public static Stream<Class<?>> owners(AnnotatedElement element) {
            return StreamSupport.stream(Spliterators.spliterator(ownerIterator(element), 0, 0), false);
        }

        public static @Nullable Class<?> owner(AnnotatedElement element) {
            return Iterators.getLast(ownerIterator(element), null);
        }
    }
}
