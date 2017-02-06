package tc.oc.api.minecraft.connectable;

import javax.inject.Provider;

import tc.oc.api.connectable.Connectable;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.event.ListenerBinder;

public class ConnectablesManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(Connector.class);
        new ListenerBinder(binder())
            .bindListener().to(Connector.class);

        final Provider<Connector> connectorProvider = getProvider(Connector.class);
        publicBinder().bindProvisionSubtypesOfListener(Connectable.class, provision -> {
            connectorProvider.get().register(provision.provision());
        });
    }
}
