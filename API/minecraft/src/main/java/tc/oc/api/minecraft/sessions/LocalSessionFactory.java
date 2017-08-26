package tc.oc.api.minecraft.sessions;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.minecraft.api.entity.Player;
import tc.oc.minecraft.protocol.MinecraftVersion;

@Singleton
public class LocalSessionFactory {

    @Inject Server localServer;
    @Inject UserStore<Player> userStore;

    public Session newSession(UserId userId, InetAddress ip) {
        final String id = UUID.randomUUID().toString();
        final Instant start = Instant.now();
        final PlayerId playerId = userStore.playerId(userId);

        return new Session() {
            @Override
            public String _id() {
                return id;
            }

            @Override
            public String family_id() {
                return localServer.family();
            }

            @Override
            public String server_id() {
                return localServer._id();
            }

            @Override
            public String version() {
                return userStore.byUserId(userId)
                                .map(player -> MinecraftVersion.describeProtocol(player.getProtocolVersion()))
                                .orElse(null);
            }

            @Override
            public PlayerId user() {
                return playerId;
            }

            @Override
            public @Nullable String nickname() {
                return null;
            }

            @Override
            public @Nullable String nickname_lower() {
                return null;
            }

            @Override
            public String ip() {
                return ip.getHostAddress();
            }

            @Override
            public Instant start() {
                return start;
            }

            @Override
            public @Nullable Instant end() {
                return null;
            }
        };
    }
}
