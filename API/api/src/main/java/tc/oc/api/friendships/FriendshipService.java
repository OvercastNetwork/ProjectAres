package tc.oc.api.friendships;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Friendship;
import tc.oc.api.docs.virtual.FriendshipDoc;
import tc.oc.api.model.ModelService;

public interface FriendshipService extends ModelService<Friendship, FriendshipDoc.Partial> {

    ListenableFuture<FriendshipResponse> create(FriendshipRequest request);

    ListenableFuture<FriendshipResponse> destroy(FriendshipRequest request);

    ListenableFuture<FriendshipResponse> list(FriendshipRequest request);

}
