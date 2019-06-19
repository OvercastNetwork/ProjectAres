package tc.oc.commons.bukkit.localization;

import com.google.common.cache.LoadingCache;
import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.commons.core.localization.Locales;
import tc.oc.commons.core.localization.LocalizedFileManager;
import tc.oc.commons.core.util.CacheUtils;
import tc.oc.file.PathWatcher;
import tc.oc.file.PathWatcherHandle;
import tc.oc.file.PathWatcherService;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.parse.ParseException;
import tc.oc.parse.xml.DocumentParser;

/**
 * Monitors a localized file that can be parsed with a {@link DocumentParser<T>} (which must have a binding)
 *
 * Each localized version of the file is monitored and parsed immediately when changes are detected.
 */
public class LocalizedDocument<T> {

    public interface Factory<T> {
        LocalizedDocument<T> create(@Assisted("source") Path source, @Assisted("localized") Path localized);
    }

    private final Path sourcePath;
    private final Path localizedPath;
    private final PathWatcherService watcherService;
    private final LocalizedFileManager localizedFiles;
    private final DocumentParser<T> parser;
    private final DocumentBuilder builder;
    private final MapdevLogger mapdevLogger;
    private final MainThreadExecutor mainThread;

    private final LoadingCache<Locale, Watcher> watchers = CacheUtils.newCache(Watcher::new);

    @Inject LocalizedDocument(@Assisted("source") Path sourcePath, @Assisted("localized") Path localizedPath, DocumentParser<T> parser, PathWatcherService watcherService, LocalizedFileManager localizedFiles, DocumentBuilder builder, MapdevLogger mapdevLogger, MainThreadExecutor mainThread) {
        this.sourcePath = sourcePath;
        this.localizedPath = localizedPath;
        this.parser = parser;
        this.watcherService = watcherService;
        this.localizedFiles = localizedFiles;
        this.builder = builder;
        this.mapdevLogger = mapdevLogger;
        this.mainThread = mainThread;
    }

    public void disable() {
        watchers.asMap().values()
                .forEach(watcher -> watcher.handle.cancel());
    }

    public Optional<T> getDefault() {
        return Optional.ofNullable(watchers.getUnchecked(Locales.DEFAULT_LOCALE).document);
    }

    public Stream<T> get(Locale locale) {
        return Stream.concat(localizedFiles.match(locale),
                             Stream.of(Locales.DEFAULT_LOCALE))
                     .map(l -> watchers.getUnchecked(l).document)
                     .filter(d -> d != null);
    }

    private class Watcher implements PathWatcher {
        final Locale locale;
        final PathWatcherHandle handle;

        @Nullable T document;

        private Watcher(Locale locale) throws IOException {
            this.locale = locale;
            final Path path = Locales.isDefault(locale)
                              ? sourcePath
                              : localizedFiles.localizedPath(locale, localizedPath);
            this.handle = watcherService.watch(path, mainThread, this);
        }

        @Override
        public void fileCreated(Path path) {
            fileModified(path);
        }

        @Override
        public void fileModified(Path path) {
            try {
                document = parser.parse(builder.parse(path.toFile()));
            } catch(SAXException | IOException | ParseException e) {
                mapdevLogger.log(Level.SEVERE, "Failed to load " + path + " for locale " + locale, e);
            }
        }

        @Override
        public void fileDeleted(Path path) {
            document = null;
        }
    }

}
