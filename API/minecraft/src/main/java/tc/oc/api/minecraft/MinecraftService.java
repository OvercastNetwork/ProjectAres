package tc.oc.api.minecraft;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Server;
import tc.oc.api.docs.virtual.ServerDoc;

/**
 * Service provided by Minecraft and Bungee servers acting as API clients.
 */
public interface MinecraftService {

    /**
     * Gets the full, perhaps stale, local server document that represents the
     * server this process is running as. Return value will be null if the
     * service is not connected.
     *
     * @return local server document
     */
    Server getLocalServer();

    Server everfreshLocalServer();

    boolean isLocalServer(ServerDoc.Identity server);

    ListenableFuture<Server> updateLocalServer(ServerDoc.Partial update);
}
