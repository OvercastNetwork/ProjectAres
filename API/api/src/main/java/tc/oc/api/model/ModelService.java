package tc.oc.api.model;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.types.PartialModelUpdate;

public interface ModelService<Complete extends Model, Partial extends PartialModel>
    extends QueryService<Complete>, UpdateService<Partial> {

    @Override
    ListenableFuture<Complete> update(String id, PartialModelUpdate<Partial> request);

    @Override
    default ListenableFuture<Complete> update(String id, Partial partial) {
        return (ListenableFuture<Complete>) UpdateService.super.update(id, partial);
    }

    @Override
    default ListenableFuture<Complete> update(Partial partial) {
        return (ListenableFuture<Complete>) UpdateService.super.update(partial);
    }
}
