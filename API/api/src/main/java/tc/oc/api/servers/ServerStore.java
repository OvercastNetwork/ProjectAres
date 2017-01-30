package tc.oc.api.servers;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import tc.oc.api.docs.Arena;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.message.types.FindRequest;
import tc.oc.api.model.ModelStore;
import tc.oc.commons.core.util.Nullables;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Maintains a local cache of all server documents, in real time over AMQP
 */
@Singleton
public class ServerStore extends ModelStore<Server> {

    private final Map<String, Server> byBungeeName = new HashMap<>();
    private final SetMultimap<ServerDoc.Role, Server> byRole = HashMultimap.create();
    private final SetMultimap<String, Server> byArenaId = HashMultimap.create();

    @Override
    protected FindRequest<Server> refreshAllRequest() {
        return new ServerSearchRequest();
    }

    public @Nullable Server tryBungeeName(String name) {
        checkArgument(!"default".equals(name), "Cannot lookup lobbies by bungee_name");
        return byBungeeName.get(name);
    }

    public Server byBungeeName(String name) {
        return Nullables.orElseThrow(
            tryBungeeName(name),
            () -> new IllegalStateException("Missing server with bungee_name '" + name + "'")
        );
    }

    public ImmutableSet<Server> byArena(Arena arena) {
        return ImmutableSet.copyOf(byArenaId.get(arena._id()));
    }

    public int countBukkitPlayers() {
        int playerCount = 0;
        for(Server server : byRole.get(ServerDoc.Role.PGM)) {
            if(server.online()) playerCount += server.num_online();
        }
        for(Server server : byRole.get(ServerDoc.Role.LOBBY)) {
            if(server.online()) playerCount += server.num_online();
        }
        return playerCount;
    }

    @Override
    protected void unindex(Server doc) {
        super.unindex(doc);
        byRole.remove(doc.role(), doc);
        if(doc.arena_id() != null) byArenaId.remove(doc.arena_id(), doc);
        if(doc.bungee_name() != null) byBungeeName.remove(doc.bungee_name());
    }

    @Override
    protected void reindex(Server doc) {
        super.reindex(doc);
        byRole.put(doc.role(), doc);
        if(doc.arena_id() != null) byArenaId.put(doc.arena_id(), doc);
        if(doc.bungee_name() != null) byBungeeName.put(doc.bungee_name(), doc);
    }
}
