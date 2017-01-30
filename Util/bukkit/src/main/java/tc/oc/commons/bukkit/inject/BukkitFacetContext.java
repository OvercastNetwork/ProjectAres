package tc.oc.commons.bukkit.inject;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;

import org.bukkit.event.EventRegistry;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.bukkit.event.targeted.TargetedEventBus;
import tc.oc.commons.core.inject.Facet;
import tc.oc.commons.core.inject.FacetContext;

public class BukkitFacetContext<F extends Facet> extends FacetContext<F> {

    private @Inject Plugin plugin;
    private @Inject EventRegistry eventRegistry;
    private @Inject TargetedEventBus targetedEventBus;

    private final Set<Listener> listeners = new HashSet<>();

    public Stream<Listener> listeners() {
        return listeners.stream();
    }

    @Override
    protected void discover(F facet) {
        super.discover(facet);

        if(facet instanceof Listener) {
            listeners.add((Listener) facet);
        }
    }

    @Override
    protected void enableFacet(F facet) throws Exception {
        super.enableFacet(facet);

        if(facet instanceof Listener) {
            eventRegistry.registerListener((Listener) facet);
            targetedEventBus.registerListener((Listener) facet);
        }
    }

    @Override
    protected void disableFacet(F facet) throws Exception {
        if(facet instanceof Listener) {
            targetedEventBus.unregisterListener((Listener) facet);
            eventRegistry.unregisterListener((Listener) facet);
        }

        super.disableFacet(facet);
    }
}
