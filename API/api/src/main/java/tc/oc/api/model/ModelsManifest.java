package tc.oc.api.model;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Key;
import com.google.inject.Provides;
import tc.oc.commons.core.inject.HybridManifest;

public class ModelsManifest extends HybridManifest implements ModelBinders {

    @Provides @Singleton @ModelSync
    ListeningExecutorService listeningModelSync(@ModelSync ExecutorService modelSync) {
        return MoreExecutors.listeningDecorator(modelSync);
    }

    @Override
    protected void configure() {
        // @ModelSync ExecutorService must be bound elsewhere
        final Key<Executor> executorKey = Key.get(Executor.class, ModelSync.class);
        final Key<ExecutorService> executorServiceKey = Key.get(ExecutorService.class, ModelSync.class);
        final Key<ListeningExecutorService> listeningExecutorServiceKey = Key.get(ListeningExecutorService.class, ModelSync.class);

        bind(executorKey).to(executorServiceKey);

        expose(executorKey);
        expose(executorServiceKey);
        expose(listeningExecutorServiceKey);

        new ModelListenerBinder(publicBinder());
    }
}
