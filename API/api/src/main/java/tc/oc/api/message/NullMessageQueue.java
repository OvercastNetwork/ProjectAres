package tc.oc.api.message;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;

import com.google.common.reflect.TypeToken;

public class NullMessageQueue implements MessageQueue {

    @Override
    public void bind(Class<? extends Message> type) {}

    @Override
    public <T extends Message> void subscribe(TypeToken<T> messageType, MessageHandler<T> handler, @Nullable Executor executor) {}

    @Override
    public void subscribe(MessageListener listener, @Nullable Executor executor) {}

    @Override
    public void unsubscribe(MessageHandler<?> handler) {}

    @Override
    public void unsubscribe(MessageListener listener) {}
}
