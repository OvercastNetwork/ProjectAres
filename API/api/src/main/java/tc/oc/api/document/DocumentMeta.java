package tc.oc.api.document;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

/**
 * Meta-info about a {@link Document} subtype, including all property {@link Accessor}s.
 */
public class DocumentMeta<T extends Document> {
    private final Class<T> type;
    private final ImmutableList<DocumentMeta<? super T>> ancestors;
    private final Class<? extends Document> baseType;
    private final ImmutableMap<String, Getter> getters;
    private final ImmutableMap<String, Setter> setters;

    public DocumentMeta(Class<T> type, List<DocumentMeta<? super T>> ancestors, Class<? extends Document> baseType, Map<String, Getter> getters, Map<String, Setter> setters) {
        this.type = type;
        this.ancestors = ImmutableList.copyOf(Iterables.concat(Collections.singleton(this), ancestors));
        this.baseType = baseType;
        this.getters = ImmutableMap.copyOf(getters);
        this.setters = ImmutableMap.copyOf(setters);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + type() + ">";
    }

    /**
     * The {@link Document} subtype described by this metadata
     */
    public Class<T> type() {
        return type;
    }

    /**
     * All ancestor {@link Document}s of this document, in resolution order.
     * This list includes every supertype of this document that is also a subtype
     * of {@link Document}, including this document itself.
     *
     * This list can include both classes and interfaces, which are flattened into
     * a single list using the C3 linearization algorithm. Classes always come before
     * interfaces in the list.
     */
    public ImmutableList<DocumentMeta<? super T>> ancestors() {
        return ancestors;
    }

    /**
     * Best concrete base class to extend when implementing this document
     * (used by the document generator)
     */
    public Class<? extends Document> baseType() {
        return baseType;
    }

    /**
     * All property getters visible on this document type,
     * including inherited getters that are not overridden.
     */
    public ImmutableMap<String, Getter> getters() {
        return getters;
    }

    /**
     * All property setters visible on this document type,
     * including inherited setters that are not overridden.
     */
    public ImmutableMap<String, Setter> setters() {
        return setters;
    }

    private static boolean isSerialized(AnnotatedElement element, boolean def) {
        final Serialize annotation = element.getAnnotation(Serialize.class);
        return annotation != null ? annotation.value() : def;
    }

    public static <T extends AnnotatedElement> Iterable<T> serializedMembers(Class<? extends Document> type, Iterable<T> members) {
        final boolean def = isSerialized(type, false);
        return Iterables.filter(members, member -> isSerialized(member, def));
    }

    public static Iterable<Method> serializedMethods(Class<? extends Document> type) {
        return serializedMembers(type, Arrays.asList(type.getDeclaredMethods()));
    }

    public static Iterable<Field> serializedFields(Class<? extends Document> type) {
        return serializedMembers(type, Arrays.asList(type.getDeclaredFields()));
    }
}
