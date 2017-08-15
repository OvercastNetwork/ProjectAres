package tc.oc.api.friendships;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Friendship;
import tc.oc.api.docs.virtual.Document;

import javax.annotation.Nullable;
import java.util.List;

@Serialize
public interface FriendshipResponse extends Document {
    boolean success();
    @Nullable String error();
    @Nullable List<Friendship> friendships();
}
