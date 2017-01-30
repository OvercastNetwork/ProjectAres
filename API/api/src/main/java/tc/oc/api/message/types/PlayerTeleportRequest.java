package tc.oc.api.message.types;

import java.util.UUID;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

@MessageDefaults.ExpirationMillis(10000)
@MessageDefaults.RoutingKey("teleport")
public class PlayerTeleportRequest implements Message {
    @Serialize public UUID player_uuid;
    @Serialize public @Nullable UUID target_player_uuid;

    private @Nullable ServerDoc.Identity target_server;

    @Serialize public void target_server(@Nullable ServerDoc.Identity server) { target_server = server; }
    @Serialize public @Nullable ServerDoc.Identity target_server() { return target_server; }

    public PlayerTeleportRequest() {}

    public PlayerTeleportRequest(UUID player_uuid, ServerDoc.Identity target_server, @Nullable UUID target_player_uuid) {
        this.player_uuid = player_uuid;
        this.target_server = target_server;
        this.target_player_uuid = target_player_uuid;
    }
}
