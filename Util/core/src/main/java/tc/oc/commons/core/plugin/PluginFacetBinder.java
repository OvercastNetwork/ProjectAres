package tc.oc.commons.core.plugin;

import com.google.inject.Binder;
import tc.oc.commons.core.inject.FacetBinder;
import tc.oc.minecraft.api.event.ListenerBinder;

/**
 * Binds {@link PluginFacet}s into a Set<PluginFacet>
 *
 *     final PluginFacetBinder facets = new PluginFacetBinder();
 *     facets.addBinding().to(Something.class);
 *     facets.addBinding().to(Whatever.class);
 *
 *     etc.
 */
public class PluginFacetBinder extends FacetBinder<PluginFacet> {

    private final ListenerBinder listeners;

    public PluginFacetBinder(Binder binder) {
        super(binder);
        this.listeners = new ListenerBinder(binder);
    }

    @Override
    public void add(Class<? extends PluginFacet> facetType) {
        super.add(facetType);
        listeners.bindListener().to(facetType);
    }
}
