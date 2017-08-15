package tc.oc.api.friendships;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Friendship;
import tc.oc.api.docs.virtual.FriendshipDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.model.NullModelService;

public class NullFriendshipService extends NullModelService<Friendship, FriendshipDoc.Partial> implements FriendshipService {

    @Override
    public ListenableFuture<FriendshipResponse> create(FriendshipRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<FriendshipResponse> destroy(FriendshipRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<FriendshipResponse> list(FriendshipRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }
}
