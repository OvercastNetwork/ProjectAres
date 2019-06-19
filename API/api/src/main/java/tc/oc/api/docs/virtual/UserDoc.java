package tc.oc.api.docs.virtual;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Instant;
import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.PlayerId;

public interface UserDoc {

    interface Partial extends PartialModel {}

    @Serialize
    interface Nickname extends Partial {
        @Nullable String nickname();
    }

    @Serialize
    interface Locale extends Partial {
        @Nullable String mc_locale();
    }

    class Flair {
        public String realm;
        public String text;
        public int priority;
    }

    @Serialize
    interface Identity extends PlayerId, Nickname {
        @Nonnull UUID uuid();
        @Nonnull List<Flair> minecraft_flair();
    }

    @Serialize
    interface Trophies extends Partial {
        List<String> trophy_ids();
    }

    @Serialize
    interface Channel extends Partial {
        @Nonnull ChatDoc.Type chat_channel();
    }

    interface License {

        @Serialize
        interface Kill extends Document {
            @Nonnull String victim_id();
            boolean friendly();
        }

        @Serialize
        interface Stats extends Partial {
            @Nonnull List<Kill> tnt_license_kills();
        }

        interface Request extends Stats {
            @Serialize @Nullable Instant requested_tnt_license_at();

            default boolean requestedTntLicense() {
                return requested_tnt_license_at() != null;
            }
        }

        interface Grant extends Stats {
            @Serialize @Nullable Instant granted_tnt_license_at();

            default boolean hasTntLicense() {
                return granted_tnt_license_at() != null;
            }
        }

        interface Complete extends Request, Grant {}
    }

    /**
     * Stuff we get from the API on login, and keep around for plugins to use
     */
    @Serialize
    interface Login extends Identity, Locale, Trophies, DefaultServer, FriendTokens, DeathScreen, License.Complete, Channel {
        int raindrops();
        int maptokens();
        int mutationtokens();
        String mc_last_sign_in_ip();
        @Nullable Date trial_expires_at();
        @Nullable Instant nickname_updated_at();
        Map<String, Map<String, Map<String, Object>>> stats_value();
        Map<String, Map<String, Boolean>> mc_permissions_by_realm();
        Map<String, Map<String, String>> mc_settings_by_profile();
        Map<String, String> classes();
        Set<PlayerId> friends();
        Map<String, List<Instant>> recent_match_joins_by_family_id(); // Reverse-chronological order
        int enemy_kills();
    }

    /**
     * Stuff we learn from the client on login, and report to the API
     */
    @Serialize
    interface ClientDetails extends Partial {
        String mc_client_version();
        String skin_blob(); // Base64 encoded thing returned from Skin#getData()
    }

    enum ResourcePackStatus {
        // MUST match org.bukit.ResourcePackStatus
        ACCEPTED, DECLINED, LOADED, FAILED
    }

    @Serialize
    interface ResourcePackResponse extends Partial {
        UserDoc.ResourcePackStatus resource_pack_status();
    }

    @Serialize
    interface DefaultServer extends Partial {
        @Nullable String default_server_id();
    }

    @Serialize
    interface DeathScreen extends Partial {
        String death_screen();
    }

    @Serialize
    interface FriendTokens extends Partial {
        int friend_tokens_limit();
        int friend_tokens_concurrent();
    }
}
