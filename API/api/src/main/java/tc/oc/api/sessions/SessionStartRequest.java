package tc.oc.api.sessions;

import java.net.InetAddress;
import javax.annotation.Nullable;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;

@Serialize
public interface SessionStartRequest extends Document {

    String server_id();

    String player_id();

    InetAddress ip();

    String version();

    @Nullable String previous_session_id();
}
