package tc.oc.api.servers;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.NullModelService;

public class NullServerService extends NullModelService<Server, ServerDoc.Partial> implements ServerService {

    @Override
    public ListenableFuture<?> doBungeeMetric(BungeeMetricRequest request) {
        return Futures.immediateFuture(null);
    }
}
