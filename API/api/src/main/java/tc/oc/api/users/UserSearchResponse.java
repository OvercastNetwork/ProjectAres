package tc.oc.api.users;

import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.Document;

public class UserSearchResponse implements Document {
    @Serialize public User user;
    @Serialize public boolean online;
    @Serialize public boolean disguised;
    @Serialize public @Nullable Session last_session;
    @Serialize public @Nullable Server last_server;

    public UserSearchResponse() {}

    public UserSearchResponse(User user, boolean online, boolean disguised, @Nullable Session last_session, @Nullable Server last_server) {
        this.user = user;
        this.online = online;
        this.disguised = disguised;
        this.last_session = last_session;
        this.last_server = last_server;
    }
}
