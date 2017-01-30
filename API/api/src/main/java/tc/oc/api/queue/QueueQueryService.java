package tc.oc.api.queue;

import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;
import tc.oc.api.model.ModelMeta;
import tc.oc.api.model.QueryService;

public class QueueQueryService<Complete extends Model> implements QueryService<Complete> {

    @Inject protected ModelMeta<Complete, ?> meta;
    @Inject private Transaction.Factory transactionFactory;

    @Override
    public TypeToken<Complete> completeType() {
        return meta.completeType();
    }

    @Override
    public ListenableFuture<FindMultiResponse<Complete>> find(FindRequest<Complete> request) {
        return transactionFactory.request(request, meta.multiResponseType());
    }
}
