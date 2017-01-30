package tc.oc.api.users;

import tc.oc.api.docs.UserId;
import tc.oc.api.docs.Server;

public class LogoutRequest {
    public final String player_id;
    public final String server_id;

    public LogoutRequest(UserId player, Server server) {
        this.player_id = player.player_id();
        this.server_id = server._id();
    }
}
