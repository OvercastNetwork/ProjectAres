package tc.oc.api.model;

import java.util.Collections;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;

public class NullQueryService<Complete extends Model> implements QueryService<Complete> {

    @Inject private ModelMeta<Complete, ?> meta;

    @Override
    public TypeToken<Complete> completeType() {
        return meta.completeType();
    }

    @Override
    public ListenableFuture<FindMultiResponse<Complete>> find(FindRequest<Complete> request) {
        return Futures.immediateFuture(Collections::emptyList);
    }
}
