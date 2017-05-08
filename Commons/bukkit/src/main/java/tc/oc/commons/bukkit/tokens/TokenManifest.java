package tc.oc.commons.bukkit.tokens;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class TokenManifest extends HybridManifest {
    @Override
    protected void configure() {
        requestStaticInjection(TokenUtil.class);

        new PluginFacetBinder(binder())
                .register(TokenCommands.class);
    }
}
