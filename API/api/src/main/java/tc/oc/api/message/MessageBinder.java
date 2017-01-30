package tc.oc.api.message;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

public class MessageBinder {

    private final Multibinder<MessageMeta<?>> messages;

    public MessageBinder(Binder binder) {
        this.messages = Multibinder.newSetBinder(binder, new Key<MessageMeta<?>>(){});
    }

    public LinkedBindingBuilder<MessageMeta<?>> addBinding() {
        return messages.addBinding();
    }

    public <T extends Message> void register(Class<T> type, String name) {
        addBinding().toInstance(new MessageMeta<>(type, name));
    }

    public <T extends Message> void register(Class<T> type) {
        register(type, type.getSimpleName());
    }
}
