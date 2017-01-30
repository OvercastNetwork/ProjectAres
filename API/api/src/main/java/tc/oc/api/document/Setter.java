package tc.oc.api.document;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.UncheckedExecutionException;
import tc.oc.api.docs.virtual.Document;

/**
 * Metadata for an accessor that can set the value of a {@link Document} property
 */
public interface Setter<T> extends Accessor<T> {

    /**
     * Set the value of this property on the given document.
     * @throws ExecutionException if a checked exception was thrown while trying to write the value,
     *                            or if the property is not accessible on the given object.
     */
    void set(Object obj, T value) throws ExecutionException;

    /**
     * Set the value of this property on the given document.
     * @throws UncheckedExecutionException if a checked exception was thrown while trying to write the value,
     *                                     or if the property is not accessible on the given object.
     */
    void setUnchecked(Object obj, T value);
}
