package tc.oc.commons.core.plugin;

import java.util.Set;

import tc.oc.commons.core.commands.CommandRegistry;
import tc.oc.commons.core.commands.Commands;
import tc.oc.commons.core.commands.NestedCommands;
import tc.oc.minecraft.api.event.Activatable;
import tc.oc.commons.core.inject.Facet;

/**
 * Something that needs to be enabled and disabled (along with a plugin).
 *
 * Each plugin has a private set of facets, configured through a {@link PluginFacetBinder}.
 * To get the instances, @Inject a {@link Set< PluginFacet >}.
 *
 * Facets are automatically enabled and disabled at the same time as the
 * plugin they are bound to.
 *
 * If a facet implements the {@link tc.oc.minecraft.api.event.Listener} interfaces,
 * it will also be registered to receive events.
 *
 * If it implements {@link Commands} or {@link NestedCommands}, it will be registered
 * through a {@link CommandRegistry}.
 *
 * Specific plugins may do other automatic things with their own facets, be we
 * don't yet have a framework for extending facets across all plugins.
 */

public interface PluginFacet extends Facet, Activatable {
}
