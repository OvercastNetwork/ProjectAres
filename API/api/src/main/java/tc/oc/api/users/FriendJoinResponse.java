package tc.oc.api.users;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface FriendJoinResponse extends Document {
    boolean authorized();
    String message();
}
