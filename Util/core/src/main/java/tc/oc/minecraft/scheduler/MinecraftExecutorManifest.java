package tc.oc.minecraft.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Key;
import com.google.inject.Provides;
import tc.oc.commons.core.concurrent.Flexecutor;
import tc.oc.commons.core.inject.HybridManifest;

/**
 * Platforms must provide these two bindings:
 *
 *     @Sync(defer=false) ExecutorService
 *     @Sync(defer=true) ExecutorService
 *
 * This module binds aliases/decorators with the same annotation:
 *     {@link Executor}
 *     {@link ListeningExecutorService}
 *     {@link Flexecutor}
 *
 * It also binds {@link SyncExecutor} and {@link MainThreadExecutor},
 * but these are deprecated, because there is no need for them to be
 * distinct interfaces.
 */
public class MinecraftExecutorManifest extends HybridManifest {

    @Override
    protected void configure() {
        // Executor -> ExecutorService
        bind(Key.get(Executor.class, Sync.immediate))
            .to(Key.get(ExecutorService.class, Sync.immediate));

        bind(Key.get(Executor.class, Sync.deferred))
            .to(Key.get(ExecutorService.class, Sync.deferred));
    }

    // ListeningExecutorService -> ExecutorService

    @Provides @Singleton @Sync(defer=false)
    ListeningExecutorService immediateListeningExecutor(@Sync(defer=false) ExecutorService executor) {
        return MoreExecutors.listeningDecorator(executor);
    }

    @Provides @Singleton @Sync(defer=true)
    ListeningExecutorService deferredListeningExecutor(@Sync(defer=true) ExecutorService executor) {
        return MoreExecutors.listeningDecorator(executor);
    }

    // Flexecutor -> ExecutorService

    @Provides @Singleton @Sync(defer=false)
    Flexecutor immediateFlexecutor(@Sync(defer=false) ExecutorService executor) {
        return executor instanceof Flexecutor ? (Flexecutor) executor
                                              : new ExecutorServiceWrapper(executor);
    }

    @Provides @Singleton @Sync(defer=true)
    Flexecutor deferredFlexecutor(@Sync(defer=true) ExecutorService executor) {
        return executor instanceof Flexecutor ? (Flexecutor) executor
                                              : new ExecutorServiceWrapper(executor);
    }

    // MainThreadExecutor -> ExecutorService

    @Provides @Singleton
    MainThreadExecutor mainThreadExecutor(@Sync(defer=false) ExecutorService executor) {
        return executor instanceof MainThreadExecutor ? (MainThreadExecutor) executor
                                                      : new ExecutorServiceWrapper(executor);
    }

    // SyncExecutor -> ExecutorService

    @Provides @Singleton
    SyncExecutor syncExecutor(@Sync(defer=true) ExecutorService executor) {
        return executor instanceof SyncExecutor ? (SyncExecutor) executor
                                                : new ExecutorServiceWrapper(executor);
    }
}
