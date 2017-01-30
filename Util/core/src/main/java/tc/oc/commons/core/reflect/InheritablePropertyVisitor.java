package tc.oc.commons.core.reflect;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;

/**
 * Find the value(s) of a heritable, shadowable property, given a function
 * to extract the property from a type. After traversal, the {@link #values()} map
 * will contain the most derived values found in the entire ancestry. No type
 * in the map will be assignable to any other type in the map.
 *
 * The given function is applied to each visited type. If the function returns a non-null value,
 * it is saved in the result map, and the visitor returns false, indicating that traversal
 * should NOT continue along the current ancestral line. If the function returns null,
 * the visitor returns true to continue traversing.
 */
public class InheritablePropertyVisitor<T> extends ReflectionVisitor {

    private final Map<Class<?>, T> values = new HashMap<>();
    private final Function<Class<?>, T> extractor;

    public InheritablePropertyVisitor(Function<Class<?>, T> extractor) {
        this.extractor = extractor;
    }

    public Map<Class<?>, T> values() {
        return values;
    }

    @Override
    public boolean visit(Class<?> cls) {
        T value = extractor.apply(cls);
        if(value != null) {
            for(Class<?> prior : values.keySet()) {
                if(cls.isAssignableFrom(prior)) {
                    // If there is already a value for a more derived type, bail out
                    return false;
                } else if(prior.isAssignableFrom(cls)) {
                    // If we find any values for less derived types, remove them
                    values.remove(prior);
                }
            }
            values.put(cls, value);
            return false;
        }
        return super.visit(cls);
    }
}
