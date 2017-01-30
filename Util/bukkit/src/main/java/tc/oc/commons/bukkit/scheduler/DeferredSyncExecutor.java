package tc.oc.commons.bukkit.scheduler;

import javax.inject.Inject;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import tc.oc.minecraft.scheduler.PluginExecutorBase;
import tc.oc.minecraft.scheduler.SyncExecutor;

/**
 * Posts to the main thread, even if already on it
 */
class DeferredSyncExecutor extends PluginExecutorBase implements SyncExecutor {

    @Inject protected Server server;

    @Override
    protected void executeInternal(Runnable command) {
        server.postToMainThread((Plugin) plugin, false, command);
    }
}
