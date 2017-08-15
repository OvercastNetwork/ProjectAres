package tc.oc.api.ocn;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Friendship;
import tc.oc.api.docs.virtual.FriendshipDoc;
import tc.oc.api.friendships.FriendshipRequest;
import tc.oc.api.friendships.FriendshipResponse;
import tc.oc.api.friendships.FriendshipService;
import tc.oc.api.http.HttpOption;
import tc.oc.api.model.HttpModelService;

import javax.inject.Singleton;

@Singleton
class OCNFriendshipService extends HttpModelService<Friendship, FriendshipDoc.Partial> implements FriendshipService {

    @Override
    public ListenableFuture<FriendshipResponse> create(FriendshipRequest request) {
        return this.client().post(collectionUri("create"), request, FriendshipResponse.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<FriendshipResponse> destroy(FriendshipRequest request) {
        return this.client().post(collectionUri("destroy"), request, FriendshipResponse.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<FriendshipResponse> list(FriendshipRequest request) {
        return this.client().post(collectionUri("list"), request, FriendshipResponse.class, HttpOption.INFINITE_RETRY);
    }

}
