package tc.oc.api.connectable;

import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginFacetBinder;

public class ConnectablesManifest extends HybridManifest {

    @Override
    protected void configure() {
        bindAndExpose(Connector.class);
        new PluginFacetBinder(binder())
            .add(Connector.class);
    }
}
