package tc.oc.commons.core.inspect;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import tc.oc.commons.core.util.ArrayUtils;
import tc.oc.commons.core.util.Chain;
import tc.oc.commons.core.util.Lazy;

public class Inspection {

    private static final Inspection DEFAULTS = new Inspection(true, false, true, true, false);
    public static Inspection defaults() { return DEFAULTS; }

    private final boolean optional, brief, deep, quote, inline;

    public Inspection(Inspectable.Inspect annotation) {
        this(annotation.optional(), annotation.brief(), annotation.deep(), annotation.quote(), annotation.inline());
    }

    public Inspection(boolean optional, boolean brief, boolean deep, boolean quote, boolean inline) {
        this.optional = optional;
        this.brief = brief;
        this.deep = deep;
        this.quote = quote;
        this.inline = inline;
    }

    public boolean optional() { return optional; }
    public boolean brief() { return brief; }
    public boolean deep() { return deep; }
    public boolean quote() { return quote; }
    public boolean inline() { return inline; }

    public boolean isPresent(Object value) {
        if(!optional()) return true;
        if(value == null) return false;
        if(value instanceof Optional && !((Optional) value).isPresent()) return false;
        if(value instanceof Collection && ((Collection) value).isEmpty()) return false;
        if(value instanceof Map && ((Map) value).isEmpty()) return false;
        if(value.getClass().isArray() && Array.getLength(value) == 0) return false;
        return true;
    }

    public boolean isCollection(Object value) {
        return deep() &&
               value != null &&
               (value instanceof Collection || value.getClass().isArray());
    }

    public Collection<?> asCollection(Object value) {
        if(value instanceof Collection) return (Collection<?>) value;
        if(value != null && value.getClass().isArray()) return ArrayUtils.asList(value);
        throw new IllegalArgumentException("Not a collection");
    }

    public Object unwrap(Object value) {
        if(deep()) {
            if(value instanceof Optional && ((Optional) value).isPresent()) {
                return unwrap(((Optional) value).get());
            }
            if(value instanceof Lazy) {
                return unwrap(((Lazy) value).get());
            }
        }
        return value;
    }

    public <R> R inspect(Inspector<R> inspector, Object value, Chain<Object> visited) {
        if(visited.containsIdentity(value)) {
            return inspector.cycle(value, visited, this);

        } else if(value instanceof InspectionException) {
            return inspector.exception((InspectionException) value, this);

        } else if(value instanceof Optional && ((Optional) value).isPresent()) {
            return inspect(inspector, ((Optional) value).get(), visited);

        } else if(value instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) value;
            return inspector.map(map,
                                 map.entrySet()
                                    .stream()
                                    .map(e -> Maps.immutableEntry(inspect(inspector, e.getKey(), visited.push(map)),
                                                                  inspect(inspector, e.getValue(), visited.push(map)))),
                                 this);

        } else if(isCollection(value)) {
            final Collection<?> collection = asCollection(value);
            return inspector.collection(collection,
                                        collection.stream().map(e -> inspect(inspector, e, visited)),
                                        this);

        } else if(value instanceof Inspectable) {
            final Inspectable inspectable = (Inspectable) value;
            if(brief()) {
                return inspector.reference(inspectable, this);
            } else {
                return inspector.inspectable(inspectable, inspectable.inspectProperties(inspector, visited), this);
            }

        } else {
            return inspector.scalar(value, this);
        }
    }
}
