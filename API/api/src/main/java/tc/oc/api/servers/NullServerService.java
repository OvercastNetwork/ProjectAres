package tc.oc.api.servers;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.types.UseServerRequest;
import tc.oc.api.message.types.UseServerResponse;
import tc.oc.api.model.NullModelService;

public class NullServerService extends NullModelService<Server, ServerDoc.Partial> implements ServerService {

    @Override
    public ListenableFuture<?> doBungeeMetric(BungeeMetricRequest request) {
        return Futures.immediateFuture(null);
    }

    @Override public ListenableFuture<UseServerResponse> requestServer(UseServerRequest request) {
        return Futures.immediateFuture(UseServerResponse.EMPTY);
    }
}
