package tc.oc.commons.bukkit.users;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import tc.oc.api.users.UserSearchResponse;

public class PlayerSearchResponse extends UserSearchResponse {

    private final @Nullable Player player;

    public PlayerSearchResponse(UserSearchResponse response, @Nullable Player player) {
        super(response.user, response.online, response.disguised, response.last_session, response.last_server);
        this.player = player;
    }

    public @Nullable Player player() {
        return player;
    }
}
