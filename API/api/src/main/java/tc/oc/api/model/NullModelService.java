package tc.oc.api.model;

import java.util.Collections;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;
import tc.oc.api.message.types.PartialModelUpdate;
import tc.oc.api.message.types.UpdateMultiRequest;
import tc.oc.api.message.types.UpdateMultiResponse;

public class NullModelService<Complete extends Model, Partial extends PartialModel> implements ModelService<Complete, Partial> {

    @Inject private ModelMeta<Complete, Partial> meta;

    @Override
    public TypeToken<Complete> completeType() {
        return meta.completeType();
    }

    @Override
    public TypeToken<Partial> partialType() {
        return meta.partialType();
    }

    @Override
    public ListenableFuture<FindMultiResponse<Complete>> find(FindRequest<Complete> request) {
        return Futures.immediateFuture(Collections::emptyList);
    }

    @Override
    public ListenableFuture<Complete> update(String id, PartialModelUpdate<Partial> request) {
        return find(id);
    }

    @Override
    public ListenableFuture<UpdateMultiResponse> updateMulti(UpdateMultiRequest<? extends Partial> request) {
        return Futures.immediateFuture(UpdateMultiResponse.EMPTY);
    }
}
