package tc.oc.api.message.types;

import com.google.common.reflect.TypeToken;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.Message;
import tc.oc.api.queue.MessageDefaults;

@MessageDefaults.RoutingKey("api_request")
public interface ModelMessage<T extends PartialModel> extends Message {
    default TypeToken<T> model() {
        return new TypeToken<T>(getClass()){};
    }
}
