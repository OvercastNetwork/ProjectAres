package tc.oc.api.servers;

import com.google.inject.multibindings.OptionalBinder;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class ServerModelManifest extends HybridManifest implements ModelBinders {
    @Override
    protected void configure() {
        bindAndExpose(ServerStore.class);

        bindModel(Server.class, ServerDoc.Partial.class, model -> {
            model.bindStore().to(ServerStore.class);
            model.bindService().to(ServerService.class);
        });

        OptionalBinder.newOptionalBinder(publicBinder(), ServerService.class);
    }
}
