package tc.oc.api.minecraft.users;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.User;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.util.Permissions;
import tc.oc.minecraft.api.entity.OfflinePlayer;

public class LocalUserDocument implements User {

    private final OfflinePlayer player;

    public LocalUserDocument(OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public String _id() {
        return player.getUniqueId().toString();
    }

    @Override
    public @Nullable String nickname() {
        return null;
    }

    @Override
    public @Nullable String mc_locale() {
        return null;
    }

    @Override
    public String player_id() {
        return _id();
    }

    @Override
    public String username() {
        return player.getLastKnownName().orElse("");
    }

    @Override
    public UUID uuid() {
        return player.getUniqueId();
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
    public String mc_last_sign_in_ip() {
        return player.onlinePlayer()
                     .map(p -> p.getAddress().getHostString())
                     .orElse("");
    }

    @Override
    public @Nullable Date trial_expires_at() {
        return null;
    }

    @Override
    public Map<String, Map<String, Boolean>> mc_permissions_by_realm() {
        return ImmutableMap.of(
            "global", ImmutableMap.of(
                Permissions.LOGIN, true
            )
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
}
