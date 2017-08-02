package tc.oc.commons.core.plugin;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.google.common.eventbus.EventBus;
import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.event.EventUtils;
import tc.oc.commons.core.util.ExceptionUtils;
import tc.oc.commons.core.util.Streams;
import tc.oc.minecraft.api.event.Enableable;
import tc.oc.minecraft.api.event.Listener;
import tc.oc.minecraft.api.plugin.Plugin;

public class PluginFacetLoader implements Enableable {

    private final Logger logger;
    private final Set<PluginFacet> facets;

    private final EventBus guavaEventBus; // Platform-neutral event bus
    private final CommandRegistry commandRegistry;

    @Inject PluginFacetLoader(Plugin plugin, Set<PluginFacet> facets, EventBus guavaEventBus, CommandRegistry commandRegistry) {
        this.logger = plugin.getLogger();
        this.facets = facets;
        this.guavaEventBus = guavaEventBus;
        this.commandRegistry = commandRegistry;
    }

    /**
     * The set of {@link PluginFacet}s belonging to this plugin.
     *
     * This method cannot be called before injection.
     */
    private Stream<PluginFacet> facets() {
        return facets.stream().filter(PluginFacet::isActive);
    }

    /**
     * Filter {@link #facets()} by type.
     *
     * This is useful when enabling/disabling custom facet types.
     */
    private <T> Stream<? extends T> facets(Class<T> type) {
        return ((Stream<? extends T>) facets().filter(type::isInstance));
    }

    @Override
    public void enable() {
        ExceptionUtils.propagate(() -> {
            facets().forEach(this::enableEventSubscriber);
            facets(Commands.class).forEach(this::registerCommands);
        });
    }

    @Override
    public void disable() {
        ExceptionUtils.propagate(() -> {
            Streams.reverseForEach(facets(), this::disableEventSubscriber);
        });
    }

    private void enableEventSubscriber(PluginFacet facet) {
        if(EventUtils.hasSubscriberMethods(facet)) {
            logger.fine("Enabling event subscriber " + facet.getClass().getName());
            guavaEventBus.register(facet);
        }
    }

    private void disableEventSubscriber(PluginFacet facet) {
        if(EventUtils.hasSubscriberMethods(facet)) {
            logger.fine("Disabling event subscriber " + facet.getClass().getName());
            guavaEventBus.unregister(facet);
        }
    }

    private void registerCommands(Commands commands) {
        logger.fine("Registering commands " + commands.getClass().getName());
        commandRegistry.register(commands.getClass());
    }
}
