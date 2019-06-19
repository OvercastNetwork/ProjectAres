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

    private final SetMultimap<String, Server> byName = HashMultimap.create();
    private final Map<String, Server> byBungeeName = new HashMap<>();
    private final SetMultimap<ServerDoc.Role, Server> byRole = HashMultimap.create();
    private final SetMultimap<ServerDoc.Network, Server> byNetwork = HashMultimap.create();
    private final SetMultimap<String, Server> byFamily = HashMultimap.create();
    private final SetMultimap<String, Server> byArenaId = HashMultimap.create();

    @Override
    protected FindRequest<Server> refreshAllRequest() {
        return new ServerSearchRequest();
    }

    public ImmutableSet<Server> byName(String name) {
        return ImmutableSet.copyOf(byName.get(name));
    }

    public ImmutableSet<Server> byNetwork(ServerDoc.Network network) {
        return ImmutableSet.copyOf(byNetwork.get(network));
    }

    public ImmutableSet<Server> byFamily(String family) {
        return ImmutableSet.copyOf(byFamily.get(family));
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

    public boolean canCommunicate(String serverIdA, String serverIdB) {
        if(serverIdA.equals(serverIdB)) return true;
        String profileA = byId(serverIdA).cross_server_profile();
        String profileB = byId(serverIdB).cross_server_profile();
        return profileA != null && profileB != null && profileA.equalsIgnoreCase(profileB);
    }

    @Override
    protected void unindex(Server doc) {
        super.unindex(doc);
        byName.remove(doc.name(), doc);
        byRole.remove(doc.role(), doc);
        if(doc.network() != null) byNetwork.remove(doc.network(), doc);
        if(doc.family() != null) byFamily.remove(doc.family(), doc);
        if(doc.arena_id() != null) byArenaId.remove(doc.arena_id(), doc);
        if(doc.bungee_name() != null) byBungeeName.remove(doc.bungee_name());
    }

    @Override
    protected void reindex(Server doc) {
        super.reindex(doc);
        byName.put(doc.name(), doc);
        byRole.put(doc.role(), doc);
        if(doc.network() != null) byNetwork.put(doc.network(), doc);
        if(doc.family() != null) byFamily.put(doc.family(), doc);
        if(doc.arena_id() != null) byArenaId.put(doc.arena_id(), doc);
        if(doc.bungee_name() != null) byBungeeName.put(doc.bungee_name(), doc);
    }
}
