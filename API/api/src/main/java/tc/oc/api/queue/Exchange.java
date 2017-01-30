package tc.oc.api.queue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListenableFuture;
import com.rabbitmq.client.BasicProperties;
import tc.oc.api.connectable.Connectable;
import tc.oc.api.message.Message;
import tc.oc.commons.core.logging.Loggers;

import static com.google.common.base.Preconditions.checkNotNull;

public class Exchange implements Connectable {

    @Singleton
    public static class Direct extends Exchange {
        Direct() {
            super("ocn.direct", "direct", true, false, false, null);
        }
    }

    @Singleton
    public static class Fanout extends Exchange {
        Fanout() {
            super("ocn.fanout", "fanout", true, false, false, null);
        }
    }

    @Singleton
    public static class Topic extends Exchange {
        Topic() {
            super("ocn.topic", "topic", true, false, false, null);
        }
    }

    protected Logger logger;
    protected @Inject QueueClient client;

    private final String name;
    private final String type;
    private final boolean durable;
    private final boolean autoDelete;
    private final boolean internal;
    private final Map<String,Object> arguments;

    public String name() { return name; }
    public String type() { return type; }
    public boolean durable() { return durable; }
    public boolean autoDelete() { return autoDelete; }
    public boolean internal() { return internal; }
    public Map<String,Object> arguments() { return arguments; }

    private Exchange(String name, String type, boolean durable, boolean autoDelete, boolean internal, Map<String,Object> arguments) {
        this.name = checkNotNull(name);
        this.type = checkNotNull(type);
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.internal = internal;
        this.arguments = arguments == null ? null : Collections.unmodifiableMap(new HashMap<>(arguments));
    }

    @Inject void init(Loggers loggers) {
        this.logger = loggers.get(getClass(), name);
    }

    @Override
    public void connect() throws IOException {
        logger.fine("Declaring " + type() + " exchange");
        client.getChannel().exchangeDeclare(name(), type(), durable(), autoDelete(), internal(), arguments());
    }

    @Override
    public void disconnect() throws IOException {}

    public void publishSync(Message message) {
        publishSync(message, null, (Publish) null);
    }

    public void publishSync(Message message, @Nullable Publish publish) {
        publishSync(message, null, publish);
    }

    public void publishSync(Message message, @Nullable BasicProperties properties) {
        publishSync(message, properties, (Publish) null);
    }

    public void publishSync(Message message, @Nullable BasicProperties properties, String routingKey) {
        publishSync(message, properties, new Publish(routingKey));
    }

    public void publishSync(Message message, @Nullable BasicProperties properties, @Nullable Publish publish) {
        client.publishSync(this, message, properties, publish);
    }

    public ListenableFuture<?> publishAsync(Message message) {
        return publishAsync(message, null, (Publish) null);
    }

    public ListenableFuture<?> publishAsync(Message message, String routingKey) {
        return publishAsync(message, null, new Publish(routingKey));
    }

    public ListenableFuture<?> publishAsync(Message message, @Nullable Publish publish) {
        return publishAsync(message, null, publish);
    }

    public ListenableFuture<?> publishAsync(Message message, @Nullable BasicProperties properties) {
        return publishAsync(message, properties, (Publish) null);
    }

    public ListenableFuture<?> publishAsync(Message message, @Nullable BasicProperties properties, String routingKey) {
        return publishAsync(message, properties, new Publish(routingKey));
    }

    public ListenableFuture<?> publishAsync(Message message, @Nullable BasicProperties properties, @Nullable Publish publish) {
        return client.publishAsync(this, message, properties, publish);
    }
}
