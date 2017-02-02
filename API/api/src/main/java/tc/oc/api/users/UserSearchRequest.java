package tc.oc.api.users;

import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.Document;
import tc.oc.minecraft.api.user.UserUtils;

public class UserSearchRequest implements Document {
    @Serialize public final String username;
    @Serialize public final @Nullable String sender_id;

    public UserSearchRequest(String username, @Nullable PlayerId sender) {
        this.username = UserUtils.sanitizeUsername(username);
        this.sender_id = sender == null ? null : sender._id();
    }
}
