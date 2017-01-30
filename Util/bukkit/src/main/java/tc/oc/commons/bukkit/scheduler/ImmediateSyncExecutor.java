package tc.oc.commons.bukkit.scheduler;

import javax.inject.Inject;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.minecraft.scheduler.PluginExecutorBase;

/**
 * Runs things immediately when called on the main thread, otherwise posts to the main thread
 */
class ImmediateSyncExecutor extends PluginExecutorBase implements MainThreadExecutor {

    @Inject protected Server server;

    @Override
    protected void executeInternal(Runnable command) {
        server.runOnMainThread((Plugin) plugin, false, command);
    }
}
