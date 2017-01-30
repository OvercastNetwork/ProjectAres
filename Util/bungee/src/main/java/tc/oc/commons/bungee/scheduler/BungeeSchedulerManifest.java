package tc.oc.commons.bungee.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Key;
import tc.oc.commons.core.inject.HybridManifest;
import tc.oc.commons.core.scheduler.SchedulerBackend;
import tc.oc.commons.core.scheduler.SchedulerBackendImpl;
import tc.oc.minecraft.scheduler.Sync;

public class BungeeSchedulerManifest extends HybridManifest {

    @Override
    protected void configure() {
        bind(SchedulerBackend.class).to(SchedulerBackendImpl.class);

        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        bind(Key.get(ExecutorService.class, Sync.immediate)).toInstance(executor);
        bind(Key.get(ExecutorService.class, Sync.deferred)).toInstance(executor);
    }
}
