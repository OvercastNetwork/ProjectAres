package tc.oc.api.model;

import java.util.Collection;
import javax.inject.Inject;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.http.HttpOption;
import tc.oc.api.message.types.PartialModelUpdate;
import tc.oc.api.message.types.UpdateMultiRequest;
import tc.oc.api.message.types.UpdateMultiResponse;
import tc.oc.commons.core.concurrent.FutureUtils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Generic base class for services that provide CRUD operations on a particular model.
 * @param <Partial>     Base type for all outgoing documents i.e. common ancestor to all of the model's interfaces
 * @param <Complete>    Type of incoming documents, i.e. the "complete" model
 */
public class HttpModelService<Complete extends Model, Partial extends PartialModel> extends HttpQueryService<Complete> implements ModelService<Complete, Partial> {

    @Inject private ModelMeta<Complete, Partial> meta;

    @Override
    public TypeToken<Partial> partialType() {
        return meta.partialType();
    }

    protected String updateMultiUri() {
        return collectionUri("update_multi");
    }

    protected String memberUri(Partial model) {
        checkArgument(model instanceof Model);
        return memberUri(((Model) model)._id());
    }

    protected String memberUri(Partial model, String action) {
        checkArgument(model instanceof Model);
        return memberUri(((Model) model)._id(), action);
    }

    @Override
    public ListenableFuture<Complete> update(String id, PartialModelUpdate<Partial> request) {
        return handleUpdate(client().put(memberUri(id), request, meta.completeType(), HttpOption.INFINITE_RETRY));
    }

    @Override
    public ListenableFuture<Complete> update(String id, Partial partial) {
        return update(id, updateRequest(partial));
    }

    @Override
    public ListenableFuture<Complete> update(Partial partial) {
        if(!(partial instanceof Model)) {
            throw new IllegalArgumentException("Partial model has no _id field");
        }
        Model model = (Model) partial;
        if(model._id() == null) {
            throw new IllegalArgumentException("_id is null");
        }
        return update(model._id(), partial);
    }

    @Override
    public <T extends Partial> ListenableFuture<UpdateMultiResponse> updateMulti(Collection<T> models) {
        return updateMulti((UpdateMultiRequest<T>) () -> models);
    }

    @Override
    public ListenableFuture<UpdateMultiResponse> updateMulti(UpdateMultiRequest<? extends Partial> request) {
        if(request.documents().isEmpty()) {
            return Futures.immediateFuture(UpdateMultiResponse.EMPTY);
        } else {
            return client().post(updateMultiUri(), request, UpdateMultiResponse.class, HttpOption.INFINITE_RETRY);
        }
    }

    protected <R extends UpdateMultiResponse, T extends Partial> ListenableFuture<R> updateMulti(Collection<T> models, Class<R> returnType) {
        return client().post(updateMultiUri(), (UpdateMultiRequest<T>) () -> models, returnType, HttpOption.INFINITE_RETRY);
    }

    protected ListenableFuture<Complete> handleUpdate(ListenableFuture<Complete> future) {
        return FutureUtils.peek(future, this::handleUpdate);
    }

    protected void handleUpdate(Complete doc) {}
}
