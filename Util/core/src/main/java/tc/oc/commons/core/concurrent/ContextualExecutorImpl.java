package tc.oc.commons.core.concurrent;

import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class ContextualExecutorImpl<C> extends AbstractContextualExecutor<C> {

    private final Supplier<C> contextSupplier;

    public ContextualExecutorImpl(Supplier<C> contextSupplier, Executor executor) {
        super(executor);
        this.contextSupplier = contextSupplier;
    }

    @Override
    protected @Nullable C context() {
        return contextSupplier.get();
    }
}
