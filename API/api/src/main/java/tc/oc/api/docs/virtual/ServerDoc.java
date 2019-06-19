package tc.oc.api.docs.virtual;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.team.Team;

public interface ServerDoc {

    interface Complete extends DeletableModel, Listing, Startup, Configuration, Bungee, Restart {
        @Override
        default String toShortString() {
            return bungee_name();
        }
    }

    interface Partial extends PartialModel {}

    /**
     * Info displayed in server listings (signs, picker, etc)
     */
    interface Listing extends Identity, Visible, Status, RestartQueuedAt, Mutation {}

    enum Role {
        BUNGEE, LOBBY, PGM, MAPDEV;
    }

    enum Network {
        PUBLIC, PRIVATE, TOURNAMENT;
    }

    /**
     * Things that are available in the config file i.e. that can't change dynamically
     */
    @Serialize
    interface Deployment extends Partial {
        String datacenter();
        String box();
        Role role();
    }

    @Serialize
    interface BungeeName extends Partial {
        @Nullable String bungee_name();
    }

    @Serialize
    interface Port extends Partial {
        Integer current_port();
    }

    @Serialize
    interface Ip extends Partial {
        String ip();
    }

    @Serialize
    interface Online extends Partial {
        boolean online();
    }

    @Serialize
    interface Dns extends Partial {
        boolean dns_enabled();
        @Nullable Instant dns_toggled_at();
    }

    @Serialize
    interface Identity extends DeletableModel, BungeeName, Deployment {
        int priority();
        @Nullable String family();
        String ip();
        String name();
        @Nullable String description();
        Network network();
        Set<String> realms();
        @Nullable String game_id();
        @Nullable String arena_id();

        default String slug() {
            return role() == Role.BUNGEE ? name() : bungee_name();
        }
    }

    enum Visibility {
        UNKNOWN, PUBLIC, PRIVATE, UNLISTED;
    }

    @Serialize
    interface Visible extends Partial {
        Visibility visibility();
    }

    /**
     * Startup info sent to the API
     */
    @Serialize
    interface Startup extends Online, Port {
        @Nullable DeployInfo deploy_info();
        Map<String, String> plugin_versions();
        Set<Integer> protocol_versions();
    }

    /**
     * Startup info received from the API
     */
    @Serialize
    interface Configuration extends Rotations {
        String domain();
        String settings_profile();
        Map<UUID, String> operators();
        @Nullable Team team();
        Set<UUID> participant_uuids();
        Map<String, Boolean> participant_permissions();
        Map<String, Boolean> observer_permissions();
        Map<String, Boolean> mapmaker_permissions();
        Visibility startup_visibility();
        boolean whitelist_enabled();
        boolean waiting_room();
        @Nullable String resource_pack_url();
        @Nullable String resource_pack_sha1();
        boolean resource_pack_fast_update();
        @Nullable String cross_server_profile();
    }

    @Serialize
    interface PlayerCounts extends Partial {
        default int min_players() { return 0; }
        int max_players();
        int num_observing();
    }

    @Serialize
    interface MatchStatus extends Partial {
        @Nullable MatchDoc current_match();
        int num_participating();
        @Nullable MapDoc next_map();
    }

    @Serialize
    interface Mutation extends Partial {
        Set<String> queued_mutations();
    }

    @Serialize
    interface Rotations extends Partial {
        List<Rotation> rotations();
    }

    @Serialize
    interface Rotation extends Document {
        String name();
        String next_map_id();
    }

    /**
     * Status sent to the API from Lobby
     */
    @Serialize
    interface StatusUpdate extends PlayerCounts {}

    /**
     * Status sent to the API from PGM
     */
    @Serialize
    interface MatchStatusUpdate extends StatusUpdate, MatchStatus {}

    /**
     * Status received from the API
     */
    @Serialize
    interface Status extends MatchStatusUpdate {
        boolean running();
        boolean online();
        int num_online();
    }

    @Serialize
    interface RestartQueuedAt extends Partial {
        @Nullable Instant restart_queued_at();
    }

    @Serialize
    interface Restart extends RestartQueuedAt {
        interface Priority {
            int LOW = -10;
            int NORMAL = 0;
            int HIGH = 10;
        }

        @Nullable String restart_reason();
        default int restart_priority() { return 0; }
    }

    @Serialize
    interface Bungee extends Dns {
        Map<UUID, String> fake_usernames();
        List<Banner> banners();
    }

    @Serialize
    interface Banner extends Document {
        String rendered();
        float weight();
    }
}
