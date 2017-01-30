package tc.oc.commons.core.inject;

import tc.oc.minecraft.api.event.Enableable;

/**
 * A facet extends some "primary" object by sharing its cardinality and lifecycle.
 * There is, at most, one instance of the facet for each instance of the primary object,
 * and the facet is enabled and disabled along with the primary object.
 *
 * A facet is registered at configuration-time using a {@link FacetBinder}
 */
public interface Facet extends Enableable {

    /**
     * Called when this listener starts listening
     */
    default void enable() {}

    /**
     * Called when this listener stops listening
     */
    default void disable() {}
}
