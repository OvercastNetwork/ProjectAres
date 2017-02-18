package tc.oc.pgm.listing;

import tc.oc.commons.core.commands.CommandBinder;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.minecraft.api.event.ListenerBinder;

public class ListingManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(ListingConfiguration.class);
        bind(ListingService.class).to(ListingServiceImpl.class);

        final ListenerBinder listeners = new ListenerBinder(binder());
        listeners.bindListener().to(PingListener.class);
        listeners.bindListener().to(ListingServiceImpl.class);

        new CommandBinder(binder())
            .register(ListingCommands.class);
    }
}
