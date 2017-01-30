package tc.oc.api.message.types;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.Document;
import tc.oc.api.docs.virtual.ServerDoc;

/**
 * Sent to the API to request changes to a server document
 */
public class ServerUpdateRequest implements Document {

    @Serialize public final ServerDoc.Partial server;

    public ServerUpdateRequest(ServerDoc.Partial server) {
        this.server = server;
    }
}
