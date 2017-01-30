package tc.oc.api.model;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import java.time.Duration;
import tc.oc.api.docs.virtual.PartialModel;
import tc.oc.api.message.types.UpdateMultiRequest;
import tc.oc.commons.core.scheduler.DebouncedTask;
import tc.oc.commons.core.scheduler.Scheduler;

public class BatchUpdaterFactory<T extends PartialModel> {

    private final UpdateService<T> service;
    private final Provider<BatchUpdateRequest> updateProvider;
    private final Scheduler scheduler;

    @Inject BatchUpdaterFactory(UpdateService<T> service, Provider<BatchUpdateRequest> updateProvider, Scheduler scheduler) {
        this.service = service;
        this.updateProvider = updateProvider;
        this.scheduler = scheduler;
    }

    public BatchUpdater<T> createBatchUpdater() {
        return createBatchUpdater(Duration.ZERO);
    }

    public BatchUpdater<T> createBatchUpdater(Duration delay) {
        return new BatchUpdaterImpl(delay);
    }

    private class BatchUpdaterImpl implements BatchUpdater<T> {

        final DebouncedTask task;
        @Nullable BatchUpdateRequest<T> batchUpdate;

        BatchUpdaterImpl(Duration delay) {
            this.task = scheduler.createDebouncedTask(delay, this::flush);
        }

        @Override
        public void flush() {
            if(batchUpdate != null) {
                final BatchUpdateRequest<T> batchUpdate = this.batchUpdate;
                this.batchUpdate = null;
                task.cancel();
                service.updateMulti((UpdateMultiRequest) batchUpdate);
            }
        }

        @Override
        public void schedule() {
            task.schedule();
        }

        @Override
        public void update(T doc) {
            if(batchUpdate == null) {
                batchUpdate = updateProvider.get();
            }
            batchUpdate.add(doc);
            schedule();
        }
    }
}
