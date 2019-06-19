package tc.oc.api.minecraft.users;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.SimplePlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.minecraft.servers.DefaultPermissions;

public class LocalUserDocument extends SimplePlayerId implements User {

    private final UUID uuid;
    private final String ip;

    LocalUserDocument(UUID uuid, String name, String ip) {
        super(uuid.toString(), uuid.toString(), name);
        this.uuid = uuid;
        this.ip = ip;
    }

    LocalUserDocument(tc.oc.minecraft.api.user.User user) {
        this(user.getUniqueId(),
             user.getName(),
             user.onlinePlayer()
                 .map(p -> p.getAddress().getHostString())
                 .orElse(""));
    }

    @Override
    public @Nullable String nickname() {
        return null;
    }

    @Override
    public Instant nickname_updated_at() {
        return null;
    }

    @Override
    public @Nullable String mc_locale() {
        return null;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public List<UserDoc.Flair> minecraft_flair() {
        return Collections.emptyList();
    }

    @Override
    public List<String> trophy_ids() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable Instant requested_tnt_license_at() {
        return null;
    }

    @Override
    public @Nullable Instant granted_tnt_license_at() {
        return Instant.EPOCH;
    }

    @Override
    public List<UserDoc.License.Kill> tnt_license_kills() {
        return Collections.emptyList();
    }

    @Override
    public int raindrops() {
        return 0;
    }

    @Override
    public int maptokens() {
        return 0;
    }

    @Override
    public int mutationtokens() {
        return 0;
    }

    @Override
    public String mc_last_sign_in_ip() {
        return ip;
    }

    @Override
    public @Nullable Date trial_expires_at() {
        return null;
    }

    @Override
    public Map<String, Map<String, Map<String, Object>>> stats_value() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Map<String, Boolean>> mc_permissions_by_realm() {
        return ImmutableMap.of(
            "global", DefaultPermissions.DEFAULT_PERMISSIONS
        );
    }

    @Override
    public Map<String, Map<String, String>> mc_settings_by_profile() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> classes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<PlayerId> friends() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, List<Instant>> recent_match_joins_by_family_id() {
        return Collections.emptyMap();
    }

    @Override
    public int enemy_kills() {
        return 0;
    }

    @Override
    public String default_server_id() {
        return null;
    }

    @Override
    public int friend_tokens_limit() {
        return 0;
    }

    @Override
    public int friend_tokens_concurrent() {
        return 1;
    }

    @Override
    public String death_screen() {
        return null;
    }

    @Override
    public ChatDoc.Type chat_channel() {
        return ChatDoc.Type.TEAM;
    }
}
