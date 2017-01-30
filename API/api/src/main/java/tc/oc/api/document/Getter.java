package tc.oc.api.document;

import java.util.concurrent.ExecutionException;

import tc.oc.api.docs.virtual.Document;

/**
 * Metadata for an accessor that can read the value of a {@link Document} property.
 */
public interface Getter<T> extends Accessor<T> {

    /**
     * Get the value of this property on the given document.
     * @throws ExecutionException if a checked exception was thrown while trying to read the value,
     *                            or if the property is not accessible on the given object.
     */
    T get(Object obj);
}
