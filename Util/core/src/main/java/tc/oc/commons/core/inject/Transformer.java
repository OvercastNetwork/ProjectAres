package tc.oc.commons.core.inject;

import javax.inject.Provider;

import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

@FunctionalInterface
public interface Transformer<T> {
    /**
     * Provide an instance of {@link T} given the upstream {@link Provider}.
     *
     * This method is not required to invoke the upstream provider, and if it doesn't,
     * then no upstream {@link Transformer}s or {@link Provider}s are called at all.
     */
    T transform(Provider<T> provider);
}
