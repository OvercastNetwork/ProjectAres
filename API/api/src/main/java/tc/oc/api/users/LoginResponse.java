package tc.oc.api.users;

import java.util.List;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Punishment;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.User;
import tc.oc.api.docs.Whisper;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface LoginResponse extends Document {

    @Nullable String kick();
    @Nullable String message();
    @Nullable String route_to_server();

    User user();
    @Nullable Session session();
    @Nullable Punishment punishment();
    List<Whisper> whispers();
    int unread_appeal_count();
}
