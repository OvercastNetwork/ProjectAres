package tc.oc.api.minecraft.logging;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class MinecraftLoggingManifest extends HybridManifest {

    @Override
    protected void configure() {
        final PluginFacetBinder facets = new PluginFacetBinder(binder());
        facets.register(LoggingCommands.class);
        facets.register(LoggingCommands.Parent.class);
        facets.register(NotOurProblemRavenFilter.class);
        facets.register(RavenServerTagger.class);
    }
}
