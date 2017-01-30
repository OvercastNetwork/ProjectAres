package tc.oc.api.servers;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.model.ModelService;

public interface ServerService extends ModelService<Server, ServerDoc.Partial> {

    ListenableFuture<?> doBungeeMetric(BungeeMetricRequest request);
}
