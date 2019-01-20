package tc.oc.api.minecraft.servers;

import java.util.Collection;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.types.*;
import tc.oc.api.model.NullModelService;
import tc.oc.api.servers.BungeeMetricRequest;
import tc.oc.api.servers.ServerService;

@Singleton
public class LocalServerService extends NullModelService<Server, ServerDoc.Partial> implements ServerService {

    @Inject private LocalServerDocument document;

    @Override
    public ListenableFuture<?> doBungeeMetric(BungeeMetricRequest request) {
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<FindMultiResponse<Server>> find(FindRequest<Server> request) {
        if(request instanceof FindMultiRequest) {
            final Collection<String> ids = ((FindMultiRequest) request).ids();
            return Futures.immediateFuture(() -> ids.contains(document._id()) ? Collections.singletonList(document)
                                                                              : Collections.emptyList());
        } else {
            return Futures.immediateFuture(() -> Collections.singletonList(document));
        }
    }

    @Override
    public ListenableFuture<Server> update(String id, PartialModelUpdate<ServerDoc.Partial> request) {
        if(document.equals(request.document())) {
            document.update(request.document());
            return Futures.immediateFuture(document);
        }
        return super.update(id, request);
    }

    @Override
    public ListenableFuture<UpdateMultiResponse> updateMulti(UpdateMultiRequest<? extends ServerDoc.Partial> request) {
        request.documents().forEach(this::update);
        return super.updateMulti(request);
    }

    @Override public ListenableFuture<UseServerResponse> requestServer(UseServerRequest request) {
        return Futures.immediateFuture(UseServerResponse.EMPTY);
    }
}
