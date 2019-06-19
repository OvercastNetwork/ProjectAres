package tc.oc.api.minecraft.servers;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.team;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.minecraft.config.MinecraftApiConfiguration;
import tc.oc.minecraft.api.user.User;
import tc.oc.minecraft.api.server.LocalServer;

@Singleton
public class LocalServerDocument extends StartupServerDocument implements Server {

    @Inject private MinecraftApiConfiguration config;
    @Inject private LocalServer minecraftServer;

    private @Nullable ServerDoc.StatusUpdate status;
    private @Nullable ServerDoc.MatchStatusUpdate matchStatus;
    private @Nullable ServerDoc.Mutation mutations;

    void update(ServerDoc.Partial doc) {
        if(doc instanceof ServerDoc.MatchStatusUpdate) {
            this.status = (ServerDoc.StatusUpdate) doc;
            this.matchStatus = (ServerDoc.MatchStatusUpdate) doc;
        } else if(doc instanceof ServerDoc.StatusUpdate) {
            this.status = (ServerDoc.StatusUpdate) doc;
            this.matchStatus = null;
        }

        if(doc instanceof ServerDoc.Mutation) {
            this.mutations = (ServerDoc.Mutation) doc;
        }
    }

    @Override
    public String _id() {
        return config.serverId();
    }

    @Override
    public String datacenter() {
        return config.datacenter();
    }

    @Override
    public String box() {
        return config.box();
    }

    @Override
    public ServerDoc.Role role() {
        return config.role();
    }

    @Override
    public @Nullable String bungee_name() {
        return "local-" + role().name().toLowerCase();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String ip() {
        return minecraftServer.getAddress().getHostString();
    }

    @Override
    public @Nullable Instant died_at() {
        return null;
    }

    @Override
    public boolean dead() {
        return false;
    }

    @Override
    public boolean alive() {
        return true;
    }

    @Override
    public boolean dns_enabled() {
        return false;
    }

    @Override
    public @Nullable Instant dns_toggled_at() {
        return null;
    }

    @Override
    public String family() {
        return role().name().toLowerCase();
    }

    @Override
    public ServerDoc.Network network() {
        return ServerDoc.Network.PUBLIC;
    }

    @Override
    public Set<String> realms() {
        return Collections.singleton("global");
    }

    @Override
    public String name() {
        switch(role()) {
            case BUNGEE: return "BungeeCord";
            case LOBBY: return "Lobby";
            case PGM: return "PGM";
            default: return "Server";
        }
    }

    @Override
    public @Nullable String description() {
        return null;
    }

    @Override
    public @Nullable String game_id() {
        return null;
    }

    @Override
    public @Nullable String arena_id() {
        return null;
    }

    @Override
    public ServerDoc.Visibility visibility() {
        return ServerDoc.Visibility.PUBLIC;
    }

    @Override
    public ServerDoc.Visibility startup_visibility() {
        return null;
    }

    @Override
    public String domain() {
        return "play.stratus.network";
    }

    @Override
    public String settings_profile() {
        return "public";
    }

    @Override
    public Map<UUID, String> operators() {
        final ImmutableMap.Builder<UUID, String> ops = ImmutableMap.builder();
        for(User op : minecraftServer.getOperators()) {
            ops.put(op.getUniqueId(), op.name().orElse("Player"));
        }
        return ops.build();
    }

    @Override
    public @Nullable team.Team team() {
        return null;
    }

    @Override
    public Set<UUID> participant_uuids() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, Boolean> participant_permissions() {
        return DefaultPermissions.PARTICIPANT_PERMISSIONS;
    }

    @Override
    public Map<String, Boolean> observer_permissions() {
        return DefaultPermissions.OBSERVER_PERMISSIONS;
    }

    @Override
    public Map<String, Boolean> mapmaker_permissions() {
        return DefaultPermissions.MAPMAKER_PERMISSIONS;
    }

    @Override
    public boolean whitelist_enabled() {
        return minecraftServer.hasWhitelist();
    }

    @Override
    public boolean waiting_room() {
        return false;
    }

    @Override
    public @Nullable String resource_pack_url() {
        return null;
    }

    @Override
    public @Nullable String resource_pack_sha1() {
        return null;
    }

    @Override
    public boolean resource_pack_fast_update() {
        return false;
    }

    @Override
    public String cross_server_profile() {
        return null;
    }

    @Override
    public Map<UUID, String> fake_usernames() {
        return Collections.emptyMap();
    }

    @Override
    public List<ServerDoc.Banner> banners() {
        return Collections.emptyList();
    }

    @Override
    public int max_players() {
        return minecraftServer.getMaxPlayers();
    }

    @Override
    public boolean running() {
        return true;
    }

    @Override
    public boolean online() {
        return true;
    }

    @Override
    public @Nullable Instant restart_queued_at() {
        return null;
    }

    @Override
    public @Nullable String restart_reason() {
        return null;
    }

    @Override
    public int num_online() {
        return minecraftServer.getOnlinePlayers().size();
    }

    @Override
    public int num_observing() {
        return status != null ? status.num_observing() : 0;
    }

    @Override
    public int num_participating() {
        return matchStatus != null ? matchStatus.num_participating() : 0;
    }

    @Override
    public @Nullable MatchDoc current_match() {
        return matchStatus != null ? matchStatus.current_match() : null;
    }

    @Override
    public @Nullable MapDoc next_map() {
        return matchStatus != null ? matchStatus.next_map() : null;
    }

    @Override
    public Set<String> queued_mutations() {
        return mutations != null ? mutations.queued_mutations() : Collections.emptySet();
    }

    @Override
    public List<ServerDoc.Rotation> rotations() {
        return Collections.emptyList();
    }
}
