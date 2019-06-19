package tc.oc.commons.bukkit.stats;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class StatsManifest extends HybridManifest {
    @Override
    protected void configure() {
        requestStaticInjection(StatsUtil.class);

        new PluginFacetBinder(binder())
                .register(StatsCommands.class);
    }
}
