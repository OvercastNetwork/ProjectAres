package tc.oc.api.queue;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

public class Consume {
    private final String name;
    private final boolean durable;
    private final boolean exclusive;
    private final boolean autoDelete;
    private final Map<String, Object> arguments;

    public Consume(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments) {
        this.name = name;
        this.durable = durable;
        this.exclusive = exclusive;
        this.autoDelete = autoDelete;
        this.arguments = arguments != null ? arguments : Collections.<String, Object>emptyMap();
    }

    public String name() {
        return name;
    }

    public boolean durable() {
        return durable;
    }

    public boolean exclusive() {
        return exclusive;
    }

    public boolean autoDelete() {
        return autoDelete;
    }

    public Map<String, Object> arguments() {
        return arguments;
    }
}
