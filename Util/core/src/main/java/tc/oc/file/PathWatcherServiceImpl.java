package tc.oc.file;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.MoreExecutors;
import tc.oc.commons.core.exception.ExceptionHandler;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.core.util.CacheUtils;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A service that watches files for changes and notifies a {@link PathWatcher}.
 */
@Singleton
public class PathWatcherServiceImpl implements PluginFacet, PathWatcherService {

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

    private final LoadingCache<FileSystem, WatchedFileSystem> fileSystems;
    private final LoadingCache<Path, WatchedFileSystem.WatchedDirectory> watchedDirs;

    @Inject private PathWatcherServiceImpl(Loggers loggers, ExceptionHandler exceptionHandler) {
        this.logger = loggers.get(getClass());
        this.exceptionHandler = exceptionHandler;

        this.fileSystems = CacheUtils.newCache(WatchedFileSystem::new);

        this.watchedDirs = CacheUtils.newCache(
            dir -> fileSystems.getUnchecked(dir.getFileSystem()).new WatchedDirectory(dir)
        );
    }

    @Override
    public void disable() {
        fileSystems.asMap().values().forEach(w -> w.stopAsync().awaitTerminated());
    }

    @Override
    public PathWatcherHandle watch(Path path, Executor executor, PathWatcher callback) throws IOException {
        path = path.toAbsolutePath();
        if(path.getNameCount() == 0) {
            throw new IllegalArgumentException("Cannot watch the root directory");
        }
        return watchedDirs.getUnchecked(path.getParent()).new WatchedPath(path, executor, callback);
    }

    private class WatchedFileSystem extends AbstractExecutionThreadService {

        final FileSystem fileSystem;
        final WatchService watchService;
        final Map<WatchKey, WatchedDirectory> dirsByKey = new ConcurrentHashMap<>();

        @Nullable Thread thread;

        WatchedFileSystem(FileSystem fileSystem) throws IOException {
            logger.fine(() -> "Watching new file system " + fileSystem);

            this.fileSystem = fileSystem;
            this.watchService = this.fileSystem.newWatchService();

            startAsync();
        }

        @Override
        protected void triggerShutdown() {
            if(thread != null) {
                thread.interrupt();
            }
        }

        @Override
        protected void startUp() throws Exception {
            thread = Thread.currentThread();
        }

        @Override
        protected void shutDown() throws Exception {
            dirsByKey.keySet().forEach(WatchKey::cancel);
        }

        @Override
        protected void run() {
            while(isRunning()) {
                try {
                    final WatchKey key = watchService.take();
                    final WatchedDirectory watchedDirectory = dirsByKey.get(key);
                    if(watchedDirectory == null) {
                        logger.warning("Cancelling unknown key " + key);
                        key.cancel();
                    } else {
                        for(WatchEvent<?> event : key.pollEvents()) {
                            watchedDirectory.dispatch((WatchEvent<Path>) event);
                        }
                        key.reset();
                    }
                } catch(InterruptedException e) {
                    // ignore, just check for termination
                }
            }
        }

        class WatchedDirectory implements PathWatcher {

            final Path dir;
            final SetMultimap<Path, WatchedPath> watchedPaths = HashMultimap.create();

            @Nullable WatchKey key;

            WatchedDirectory(Path dir) throws IOException {
                logger.fine(() -> "Watching new directory " + dir);

                this.dir = dir;

                // Watch ourselves, unless we are the root dir
                if(dir.getParent() != null) {
                    watch(dir, MoreExecutors.sameThreadExecutor(), this);
                }
            }

            void enable() {
                if(key == null || !key.isValid()) {
                    logger.fine(() -> "Enabling watched directory " + dir);

                    exceptionHandler.run(() -> {
                        key = this.dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                        dirsByKey.put(key, this);
                    });
                }
            }

            void disable() {
                if(key != null) {
                    logger.fine(() -> "Disabling watched directory " + dir);

                    dirsByKey.remove(key);
                    key.cancel();
                }
            }

            void cancel() {
                logger.fine(() -> "Cancelling watched directory " + dir);

                disable();
                watchedDirs.invalidate(dir);
            }

            @Override
            public void fileCreated(Path path) {
                enable();
            }

            @Override
            public void fileDeleted(Path path) {
                disable();
            }

            void dispatch(WatchEvent<Path> event) {
                for(WatchedPath watchedPath : watchedPaths.get(dir.resolve(event.context()))) {
                    watchedPath.dispatch(event.kind());
                }
            }

            class WatchedPath implements PathWatcherHandle {

                final Path path;
                final PathWatcher callback;
                final Executor executor;

                public WatchedPath(Path path, Executor executor, PathWatcher callback) {
                    logger.fine(() -> "Watching new path " + path);

                    this.path = path;
                    this.executor = executor;
                    this.callback = callback;

                    watchedPaths.put(path, this);
                    dispatch(Files.exists(path) ? ENTRY_CREATE
                                                : ENTRY_DELETE);
                }

                @Override
                public void cancel() {
                    logger.fine(() -> "Cancelling watched path " + path);

                    watchedPaths.remove(path, this);
                    if(watchedPaths.isEmpty()) {
                        WatchedDirectory.this.cancel();
                    }
                }

                void dispatch(WatchEvent.Kind<Path> kind) {
                    logger.fine(() -> "Dispatching event " + kind + " for path " + path);

                    exceptionHandler.run(() -> executor.execute(() -> {
                        if(ENTRY_CREATE.equals(kind)) {
                            callback.fileCreated(path);
                        } else if(ENTRY_MODIFY.equals(kind)) {
                            callback.fileModified(path);
                        } else if(ENTRY_DELETE.equals(kind)) {
                            callback.fileDeleted(path);
                        }
                    }));
                }
            }
        }
    }
}
