package tc.oc.api.minecraft.servers;

import javax.annotation.Nullable;

import tc.oc.api.docs.Server;
import tc.oc.api.minecraft.MinecraftService;

/**
 * Fired by {@link MinecraftService} when the local server document changes.
 */
public class LocalServerReconfigureEvent {

    protected final @Nullable Server oldConfig;
    protected final Server newConfig;

    public LocalServerReconfigureEvent(@Nullable Server oldConfig, Server newConfig) {
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public @Nullable Server getOldConfig() {
        return oldConfig;
    }

    public Server getNewConfig() {
        return newConfig;
    }
}
