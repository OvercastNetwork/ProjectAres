package tc.oc.commons.core.inspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import tc.oc.commons.core.stream.BiStream;
import tc.oc.commons.core.util.Chain;
import tc.oc.commons.core.util.Optionals;

public interface Inspectable {

    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Inspect {
        /**
         * Override property name
         */
        String name() default  "";

        /**
         * Exclude nulls and empty {@link Optional}s
         */
        boolean optional() default true;

        /**
         * Expand arrays and collections
         */
        boolean deep() default true;

        /**
         * Only identify, don't inspect
         */
        boolean brief() default false;

        /**
         * Quote string/character values
         */
        boolean quote() default true;

        /**
         * Flatten child properties into parent
         */
        boolean inline() default false;
    }

    default String identify() {
        return Optionals.reduce(inspectType(), inspectIdentity(), (type, id) -> type + ":" + id);
    }

    default String inspect() {
        final TextInspector inspector = new TextInspector();
        return inspect(inspector, Inspection.defaults());
    }

    default <R> R inspect(Inspector<R> inspector, Inspection options) {
        return options.inspect(inspector, this, Chain.empty());
    }

    default String inspectType() {
        return getClass().getSimpleName();
    }

    default Optional<String> inspectIdentity() {
        return Optional.empty();
    }

    default <R> BiStream<String, R> inspectProperties(Inspector<R> inspector, Chain<Object> visited) {
        return BiStream.from(inspectableProperties().flatMap(property -> property.flatValues(this)))
                       .map((property, value) -> property.name(),
                            (property, value) -> property.options().inspect(inspector, value, visited.push(this)));
    }

    default Stream<? extends InspectableProperty> inspectableProperties() {
        return ReflectiveProperty.all(getClass()).stream();
    }

    class Impl implements Inspectable {
        @Override public String toString() {
            return inspect();
        }
    }
}
