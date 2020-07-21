package tc.oc.api.message;

import com.google.common.reflect.TypeToken;

public interface MessageHandler<T extends Message> {
    void handleDelivery(T message, TypeToken<? extends T> type);
}
