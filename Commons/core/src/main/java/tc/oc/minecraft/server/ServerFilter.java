package tc.oc.minecraft.server;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.commons.core.stream.Collectors;

/**
 * General purpose object to filter server documents by various criteria.
 *
 * To match, every non-empty field of the filter must have a non-empty
 * intersection with the respective field of the server document.
 * In other words, different fields are combined with AND, but members
 * of the same field are combined with OR, and empty fields are ignored.
 *
 * @see ServerFilterParser
 */
public class ServerFilter extends Inspectable.Impl implements Predicate<ServerDoc.Identity> {

    @Inspect private final ImmutableSet<ServerDoc.Role> roles;
    @Inspect private final ImmutableSet<ServerDoc.Network> networks;
    @Inspect private final ImmutableSet<String> realms;
    @Inspect private final ImmutableSet<String> games;
    @Inspect private final ImmutableSet<MapDoc.Gamemode> gamemodes;

    public ServerFilter(Stream<ServerDoc.Role> roles, Stream<ServerDoc.Network> networks, Stream<String> realms, Stream<String> games, Stream<MapDoc.Gamemode> gamemodes) {
        this.roles = roles.collect(Collectors.toImmutableSet());
        this.networks = networks.collect(Collectors.toImmutableSet());
        this.realms = realms.collect(Collectors.toImmutableSet());
        this.games = games.collect(Collectors.toImmutableSet());
        this.gamemodes = gamemodes.collect(Collectors.toImmutableSet());
    }

    /**
     * @see ServerDoc.Role
     * @see ServerDoc.Identity#role()
     */
    public ImmutableSet<ServerDoc.Role> roles() {
        return roles;
    }

    /**
     * @see ServerDoc.Network
     * @see ServerDoc.Identity#network()
     */
    public ImmutableSet<ServerDoc.Network> networks() {
        return networks;
    }

    /**
     * @see ServerDoc.Identity#realms()
     */
    public ImmutableSet<String> realms() {
        return realms;
    }

    /**
     * @see ServerDoc.Identity#game_id()
     */
    public ImmutableSet<String> games() {
        return games;
    }

    /**
     * @see MapDoc.Gamemode
     * @see ServerDoc.MatchStatus#current_match()#gamemodes()
     */
    public ImmutableSet<MapDoc.Gamemode> gamemodes() {
        return gamemodes;
    }

    @Override
    public boolean test(ServerDoc.Identity server) {
        if(!roles.isEmpty() && !roles.contains(server.role())) return false;
        if(!networks.isEmpty() && !networks.contains(server.network())) return false;
        if(!realms.isEmpty() && Sets.intersection(realms, server.realms()).isEmpty()) return false;
        if(!games.isEmpty() && !games.contains(server.game_id())) return false;

        if(!gamemodes.isEmpty()) {
            if(!(server instanceof ServerDoc.MatchStatus)) return false;
            final MatchDoc match = ((ServerDoc.MatchStatus) server).current_match();
            if(match == null) return false;
            if(Sets.intersection(gamemodes, match.map().gamemode()).isEmpty()) return false;
        }

        return true;
    }
}
