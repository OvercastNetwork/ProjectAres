package tc.oc.api.message;

import com.google.common.reflect.TypeToken;
import tc.oc.api.queue.Delivery;
import tc.oc.api.queue.Metadata;

public interface MessageHandler<T extends Message> {
    void handleDelivery(T message, TypeToken<? extends T> type, Metadata properties, Delivery delivery);
}
