package tc.oc.commons.bukkit.broadcast.model;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.minecraft.server.ServerFilter;

/**
 * A periodic broadcast selected randomly from a set of localized messages
 */
public class BroadcastSchedule extends Inspectable.Impl {

    @Inspect private final Duration delay;
    @Inspect private final Duration interval;
    @Inspect private final ImmutableList<BroadcastSet> messages;
    @Inspect private final ServerFilter serverFilter;

    public BroadcastSchedule(Duration delay, Duration interval, ServerFilter serverFilter, Stream<BroadcastSet> messages) {
        this.delay = delay;
        this.interval = interval;
        this.serverFilter = serverFilter;
        this.messages = messages.collect(Collectors.toImmutableList());
    }

    /**
     * Time between broadcasts
     */
    public Duration interval() {
        return interval;
    }

    /**
     * Time before first broadcast
     */
    public Duration delay() {
        return delay;
    }

    /**
     * Relative path of the localized message list.
     *
     * This path is be relative to the localized root, and must NOT have an extension.
     */
    public List<BroadcastSet> messages() {
        return messages;
    }

    public ServerFilter serverFilter() {
        return serverFilter;
    }
}
