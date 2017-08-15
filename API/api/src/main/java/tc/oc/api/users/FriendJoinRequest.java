package tc.oc.api.users;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

import javax.annotation.Nullable;
import java.time.Instant;

@Serialize
public interface FriendJoinRequest extends Document {
    int amount();
}
