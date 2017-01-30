package tc.oc.api.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Ticket;
import tc.oc.api.model.ModelStore;

@Singleton
public class TicketStore extends ModelStore<Ticket> {

    private final Map<PlayerId, Ticket> byUser = new HashMap<>();
    private final SetMultimap<String, Ticket> byArenaId = HashMultimap.create();
    private final SetMultimap<String, Ticket> byArenaIdQueued = HashMultimap.create();

    public @Nullable Ticket tryUser(PlayerId playerId) {
        return byUser.get(playerId);
    }

    public Set<Ticket> byArena(Arena arena) {
        return byArenaId.get(arena._id());
    }

    public Set<Ticket> queued(Arena arena) {
        return byArenaIdQueued.get(arena._id());
    }

    @Override
    protected void reindex(Ticket doc) {
        super.reindex(doc);
        byUser.put(doc.user(), doc);
        byArenaId.put(doc.arena_id(), doc);
        if(doc.server_id() == null) {
            byArenaIdQueued.put(doc.arena_id(), doc);
        } else {
            byArenaIdQueued.remove(doc.arena_id(), doc);
        }
    }

    @Override
    protected void unindex(Ticket doc) {
        super.unindex(doc);
        byUser.remove(doc.user());
        byArenaId.remove(doc.arena_id(), doc);
        byArenaIdQueued.remove(doc.arena_id(), doc);
    }
}
