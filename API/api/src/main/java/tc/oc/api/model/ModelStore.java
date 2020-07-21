package tc.oc.api.model;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.connectable.Connectable;
import tc.oc.api.docs.virtual.DeletableModel;
import tc.oc.api.docs.virtual.Model;
import tc.oc.api.document.DocumentGenerator;
import tc.oc.api.message.MessageListener;
import tc.oc.api.message.MessageService;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.message.types.FindRequest;
import tc.oc.api.message.types.ModelDelete;
import tc.oc.api.message.types.ModelUpdate;
import tc.oc.commons.core.concurrent.FutureUtils;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.commons.core.util.ProxyUtils;
import tc.oc.minecraft.suspend.Suspendable;

public abstract class ModelStore<T extends Model> implements MessageListener, Connectable, Suspendable {

    protected Logger logger;
    protected @Inject QueryService<T> queryService;
    protected @Inject
    MessageService primaryQueue;
    protected @Inject @ModelSync ExecutorService modelSync;
    protected @Inject ModelDispatcher dispatcher;

    protected ModelMeta<T, ? super T> meta;
    private LoadingCache<String, T> proxies;

    private final Map<String, T> byId = new HashMap<>();

    @Inject void init(Loggers loggers, ModelRegistry registry) {
        this.logger = loggers.get(getClass());
        this.meta = (ModelMeta<T, ? super T>) registry.meta(new TypeToken<T>(getClass()){});

        this.proxies = CacheUtils.newCache(id -> ProxyUtils.newProviderProxy(meta.completeTypeRaw(), () -> byId(id)));
    }

    @Override
    public void connect() throws IOException {
        primaryQueue.bind(ModelUpdate.class);
        primaryQueue.bind(ModelDelete.class);

        primaryQueue.subscribe(this, modelSync);
        refreshAllSync();
    }

    @Override
    public void disconnect() throws IOException {
        primaryQueue.unsubscribe(this);
    }

    @Override
    public void resume() {
        refreshAll();
    }

    /**
     * Return a transparent proxy for the stored document with the given ID.
     * Every method call on the proxy is forwarded to the version of the document
     * stored at the time of the call.
     *
     * The document does not need to actually exist when the proxy is created,
     * but it must exist any time a method is called.
     *
     * TODO: Avoid double-wrapping. In most cases, this will return a proxy to
     * another proxy. The inner proxy is from {@link DocumentGenerator} to implement
     * the document interface, and the outer proxy is the one created here to look
     * it up in the tracker. Would be nice if these were combined into a single proxy.
     */
    public T proxy(String id) {
        return proxies.getUnchecked(id);
    }

    /**
     * Return the stored document with the given ID
     *
     * @throws IllegalStateException if there is no stored document with the given ID
     */
    public T byId(String id) {
        final T doc = byId.get(id);
        if(doc == null) {
            throw new IllegalStateException("Missing " + meta.name() + " " + id);
        }
        return doc;
    }

    public Optional<T> tryId(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public int count() {
        return byId.size();
    }

    public Stream<T> all() {
        return byId.values().stream();
    }

    public Set<T> set() {
        return ImmutableSet.copyOf(byId.values());
    }

    public Set<T> subset(Predicate<? super T> filter) {
        return byId.values().stream().filter(filter).collect(Collectors.toSet());
    }

    public @Nullable T first(Comparator<? super T> order, @Nullable Predicate<? super T> filter) {
        T min = null;
        for(T doc : byId.values()) {
            if((filter == null || filter.test(doc)) && (min == null || order.compare(doc, min) < 0)) {
                min = doc;
            }
        }
        return min;
    }

    protected void logAction(String verb, T model) {
        if(logger.isLoggable(Level.FINE)) {
            logger.fine(verb + ' ' + meta.name() + ' ' + model._id());
        }
    }

    protected FindRequest<T> refreshAllRequest() {
        return new FindRequest<>(meta.completeType());
    }

    protected ListenableFuture<FindMultiResponse<T>> sendRefreshAll() {
        logger.fine("Requesting refresh of all documents");
        return queryService.find(refreshAllRequest());
    }

    public ListenableFuture<FindMultiResponse<T>> refreshAll() {
        return FutureUtils.mapSync(
            sendRefreshAll(),
            response -> handleRefreshAll(response, Runnable::run),
            modelSync
        );
    }

    /**
     * Must be called on the ModelSync thread
     */
    protected FindMultiResponse<T> refreshAllSync() {
        // At startup, defer notifications to the next tick, after all ModelStores are populated,
        // because some handlers will try to lookup foreign keys across stores.
        return handleRefreshAll(Futures.getUnchecked(sendRefreshAll()), modelSync);
    }

    protected FindMultiResponse<T> handleRefreshAll(FindMultiResponse<T> response, Executor notificationExecutor) {
        logger.fine(() -> "Storing " + response.documents().size() + " documents");
        response.documents().forEach(after -> handleUpdate(after._id(), after, notificationExecutor));
        final Set<T> gone = new HashSet<>(byId.values());
        gone.removeAll(response.documents());
        gone.forEach(doc -> handleUpdate(doc._id(), null, notificationExecutor));
        return response;
    }

    @HandleMessage
    public void onUpdate(ModelUpdate<T> message) {
        handleUpdate(message.document()._id(), message.document());
    }

    @HandleMessage
    public void onDelete(ModelDelete<T> message) {
        handleUpdate(message.document_id(), null);
    }

    private boolean exists(@Nullable T doc) {
        return doc != null && !(doc instanceof DeletableModel && ((DeletableModel) doc).dead());
    }

    private void handleUpdate(String id, @Nullable T after) {
        handleUpdate(id, after, Runnable::run);
    }

    private void handleUpdate(String id, @Nullable T after, Executor notificationExecutor) {
        final T before = byId.get(id);
        final T latest;

        if(exists(before)) {
            if(exists(after)) {
                logAction("Update", after);
                unindex(before);
                reindex(after);
                latest = after;
            } else {
                logAction("Delete", before);
                unindex(before);
                remove(before);
                latest = before;
            }
        } else if(exists(after)) {
            logAction("Create", after);
            reindex(after);
            latest = after;
        } else {
            latest = null;
        }

        if(exists(latest)) {
            notificationExecutor.execute(() -> dispatcher.modelUpdated(before, after, latest));
        }
    }

    /**
     * Called only when a document is deleted, after {@link #unindex}.
     *
     * Subclasses only need to override this method if they need to do some extra
     * cleanup, besides unindexing.
     */
    protected void remove(T doc) {
        proxies.invalidate(doc._id());
    }

    /**
     * Called when a document is updated, or deleted. The argument is the state
     * of the document before the change.
     *
     * Subclasses can override this in order to maintain their own indexes.
     */
    protected void unindex(T doc) {
        byId.remove(doc._id());
    }

    /**
     * This method is called when a document is created or updated. The argument
     * is the state of the document after the change.
     *
     * Subclasses can override this in order to maintain their own indexes.
     */
    protected void reindex(T doc) {
        byId.put(doc._id(), doc);
    }
}
