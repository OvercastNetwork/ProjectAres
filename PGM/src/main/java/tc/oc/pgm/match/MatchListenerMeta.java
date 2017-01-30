package tc.oc.pgm.match;

import javax.annotation.Nullable;

import org.bukkit.event.Listener;

public class MatchListenerMeta {
    private final Class<? extends Listener> type;
    private final @Nullable MatchScope scope;

    public MatchListenerMeta(Class<? extends Listener> type, @Nullable MatchScope scope) {
        this.type = type;
        this.scope = scope;
    }

    public Class<? extends Listener> type() {
        return type;
    }

    public @Nullable MatchScope scope() {
        return scope;
    }
}
