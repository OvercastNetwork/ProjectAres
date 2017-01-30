package tc.oc.api.message.types;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

@Serialize
@MessageDefaults.RoutingKey("match_maker")
@MessageDefaults.Persistent(false)
@MessageDefaults.ExpirationMillis(10000)
public interface CycleRequest extends Message {
    String server_id();
    String map_id();
    int min_players();
    int max_players();
}
