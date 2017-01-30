package tc.oc.api.users;

import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface UserUpdateResponse extends Document {

    boolean success();
    @Nullable User user();

    UserUpdateResponse FAILURE = new UserUpdateResponse() {
        @Override
        public boolean success() {
            return false;
        }

        @Override
        public @Nullable User user() {
            return null;
        }
    };
}
