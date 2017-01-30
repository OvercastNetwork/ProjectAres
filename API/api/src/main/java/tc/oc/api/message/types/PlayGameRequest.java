package tc.oc.api.message.types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

@Serialize
@MessageDefaults.RoutingKey("match_maker")
@MessageDefaults.Persistent(false)
@MessageDefaults.ExpirationMillis(10000)
public interface PlayGameRequest extends Message {
    @Nonnull String user_id();
    @Nullable String arena_id();
}
