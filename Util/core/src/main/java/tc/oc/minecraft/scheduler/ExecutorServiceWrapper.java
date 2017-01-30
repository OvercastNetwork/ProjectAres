package tc.oc.minecraft.scheduler;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.ForwardingListeningExecutorService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

class ExecutorServiceWrapper extends ForwardingListeningExecutorService implements MainThreadExecutor, SyncExecutor {

    private final ListeningExecutorService delegate;

    public ExecutorServiceWrapper(ExecutorService delegate) {
        this.delegate = MoreExecutors.listeningDecorator(delegate);
    }

    @Override
    protected ListeningExecutorService delegate() {
        return delegate;
    }
}
