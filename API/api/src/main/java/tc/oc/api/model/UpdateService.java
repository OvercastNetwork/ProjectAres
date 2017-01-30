package tc.oc.api.model;

import java.util.Collection;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.types.PartialModelUpdate;
import tc.oc.api.message.types.UpdateMultiRequest;
import tc.oc.api.message.types.UpdateMultiResponse;

public interface UpdateService<Partial extends PartialModel> {

    TypeToken<Partial> partialType();

    default PartialModelUpdate<Partial> updateRequest(Partial document) {
        return new PartialModelUpdate<Partial>() {
            @Override public Partial document() { return document; }
            @Override public TypeToken<Partial> model() { return partialType(); }
        };
    }

    ListenableFuture<?> update(String id, PartialModelUpdate<Partial> request);

    default ListenableFuture<?> update(String id, Partial partial) {
        return update(id, updateRequest(partial));
    }

    default ListenableFuture<?> update(Partial partial) {
        if(!(partial instanceof Model)) {
            throw new IllegalArgumentException("Partial model has no _id field");
        }
        Model model = (Model) partial;
        if(model._id() == null) {
            throw new IllegalArgumentException("_id is null");
        }
        return update(model._id(), partial);
    }

    default <T extends Partial> ListenableFuture<UpdateMultiResponse> updateMulti(Collection<T> models) {
        return updateMulti((UpdateMultiRequest<T>) () -> models);
    }

    ListenableFuture<UpdateMultiResponse> updateMulti(UpdateMultiRequest<? extends Partial> request);
}
