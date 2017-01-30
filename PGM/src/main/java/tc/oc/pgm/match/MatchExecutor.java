package tc.oc.pgm.match;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tc.oc.minecraft.scheduler.SyncExecutor;
import tc.oc.commons.core.concurrent.AbstractContextualExecutor;
import tc.oc.commons.core.concurrent.ContextualExecutor;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchUnloadEvent;

/**
 * A {@link ContextualExecutor} with a {@link Match} as the context.
 *
 * Tasks are silently dropped after the match unloads.
 */
@ListenerScope(MatchScope.LOADED)
public class MatchExecutor extends AbstractContextualExecutor<Match> implements Listener {

    private @Nullable Match match;

    @Inject MatchExecutor(SyncExecutor executor, Match match) {
        super(executor);

        if(!match.isUnloaded()) {
            this.match = match;
            this.match.registerEvents(this);
        }
    }

    @Override
    protected @Nullable Match context() {
        return match;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void unload(MatchUnloadEvent event) {
        if(event.getMatch().equals(match)) {
            match = null;
        }
    }
}
