package tc.oc.api.friendships;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

import javax.annotation.Nullable;

@Serialize
public interface FriendshipRequest extends Document {
    String friender_id();
    @Nullable String friended_id();

    static FriendshipRequest create(String friender_id, @Nullable String friended_id) {
        return new FriendshipRequest() {
            public String friender_id() { return friender_id; }
            public String friended_id() { return friended_id; }
        };
    }
}
