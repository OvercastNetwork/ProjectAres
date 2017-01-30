package tc.oc.commons.bukkit.scheduler;

import java.util.concurrent.ExecutorService;

import com.google.inject.Key;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.plugin.PluginScoped;
import tc.oc.commons.core.scheduler.SchedulerBackend;
import tc.oc.minecraft.scheduler.Sync;

public class BukkitSchedulerManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(ImmediateSyncExecutor.class).in(PluginScoped.class);
        bind(DeferredSyncExecutor.class).in(PluginScoped.class);

        bind(Key.get(ExecutorService.class, Sync.immediate)).to(ImmediateSyncExecutor.class);
        bind(Key.get(ExecutorService.class, Sync.deferred)).to(DeferredSyncExecutor.class);

        bind(SchedulerBackend.class).to(BukkitSchedulerBackend.class);
    }
}
