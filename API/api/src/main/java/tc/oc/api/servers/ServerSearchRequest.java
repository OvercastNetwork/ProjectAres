package tc.oc.api.servers;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.Server;
import tc.oc.api.message.types.FindRequest;

public class ServerSearchRequest extends FindRequest<Server> {

    @Serialize private final boolean offline = true;
    @Serialize private final boolean unlisted = true;
}
