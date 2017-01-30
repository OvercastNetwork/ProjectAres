package tc.oc.api.model;

import java.util.Collection;
import java.util.Collections;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.message.types.FindMultiRequest;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;
import tc.oc.commons.core.concurrent.FutureUtils;

public interface QueryService<Complete extends Model> {

    TypeToken<Complete> completeType();

    ListenableFuture<FindMultiResponse<Complete>> find(FindRequest<Complete> request);

    default ListenableFuture<FindMultiResponse<Complete>> all() {
        return find(new FindRequest<>(completeType()));
    }

    default ListenableFuture<Complete> find(String id) {
        return FutureUtils.mapAsync(
            find(Collections.singleton(id)),
            response -> response.documents().stream().findAny()
                                .map(Futures::immediateFuture)
                                .orElseGet(() -> Futures.immediateFailedFuture(new NotFound(completeType().toString() + " with id " + id, null)))
        );
    }

    default ListenableFuture<FindMultiResponse<Complete>> find(Collection<String> ids) {
        return find(new FindMultiRequest<>(completeType(), ids));
    }
}
