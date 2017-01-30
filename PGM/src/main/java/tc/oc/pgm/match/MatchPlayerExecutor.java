package tc.oc.pgm.match;

import java.util.UUID;
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
 * A {@link ContextualExecutor} with the {@link MatchPlayer} as context.
 *
 * Tasks only execute if the {@link Match} is loaded, and it has a {@link MatchPlayer}
 * with the same {@link UUID} as the original player.
 */
@ListenerScope(MatchScope.LOADED)
public class MatchPlayerExecutor extends AbstractContextualExecutor<MatchPlayer> implements MatchPlayerFacet, Listener {

    private @Nullable Match match;
    private final UUID uuid;

    @Inject MatchPlayerExecutor(SyncExecutor syncExecutor, Match match, UUID uuid) {
        super(syncExecutor);
        this.uuid = uuid;

        if(!match.isUnloaded()) {
            this.match = match;
            this.match.registerEvents(this);
        }
    }

    @Override
    protected @Nullable MatchPlayer context() {
        return match == null ? null : match.getPlayer(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void unload(MatchUnloadEvent event) {
        if(event.getMatch().equals(match)) {
            match = null;
        }
    }
}
