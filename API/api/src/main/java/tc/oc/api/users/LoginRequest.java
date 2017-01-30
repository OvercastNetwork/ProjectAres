package tc.oc.api.users;

import tc.oc.api.docs.Server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import javax.annotation.Nullable;

public class LoginRequest {
    public final String username;
    public final @Nullable UUID uuid;
    public final InetAddress ip;
    public final String server_id;
    public final String virtual_host;
    public final boolean start_session;
    public final @Nullable String mc_client_version;

    public LoginRequest(String username, @Nullable UUID uuid, InetAddress ip, Server server, boolean start_session) {
        this(username, uuid, ip, server, null, start_session);
    }

    public LoginRequest(String username, @Nullable UUID uuid, InetAddress ip, Server server, InetSocketAddress virtual_host, boolean start_session) {
        this(username, uuid, ip, server, virtual_host, start_session, null);
    }

    public LoginRequest(String username, @Nullable UUID uuid, InetAddress ip, Server server, InetSocketAddress virtual_host, boolean start_session, @Nullable String mc_client_version) {
        this.username = username;
        this.uuid = uuid;
        this.ip = ip;
        this.server_id = server._id();
        this.virtual_host = virtual_host == null ? null : virtual_host.getHostName();
        this.start_session = start_session;
        this.mc_client_version = mc_client_version;
    }
}
