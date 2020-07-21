package tc.oc.api.message;

import com.google.common.reflect.TypeToken;

import javax.inject.Singleton;

@Singleton
public class LocalMessageService extends AbstractMessageService {

    @Override
    public void bind(Class<? extends Message> type) {}
    
    public void receive(Message message, TypeToken<? extends Message> type) {
        dispatchMessage(message, type);
    }
    
    public void receive(Message message) {
        dispatchMessage(message, TypeToken.of(message.getClass()));
    }

}
