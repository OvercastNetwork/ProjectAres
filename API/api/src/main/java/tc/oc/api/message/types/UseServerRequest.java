package tc.oc.api.message.types;

import javax.annotation.Nonnull;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

@Serialize
@MessageDefaults.RoutingKey("use_server")
@MessageDefaults.ExpirationMillis(10000)
public interface UseServerRequest extends Message {
    @Nonnull String user_id();
}
