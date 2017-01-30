package tc.oc.parse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;

import com.google.inject.assistedinject.Assisted;
import org.xml.sax.SAXException;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.minecraft.scheduler.MainThreadExecutor;
import tc.oc.file.PathWatcher;
import tc.oc.file.PathWatcherHandle;
import tc.oc.file.PathWatcherService;
import tc.oc.parse.xml.DocumentParser;

/**
 * Watches a single file that is parseable with a bound {@link DocumentParser<T>}.
 *
 * When the file changes, it is parsed and passed to the given {@link Consumer}.
 * If the file is removed, the consumer is called with an empty value.
 */
public class DocumentWatcher<T> implements PathWatcher {

    public interface Factory<T> {
        DocumentWatcher<T> create(Path path, Consumer<Optional<T>> callback);
    }

    private final DocumentBuilder builder;
    private final DocumentParser<T> parser;
    private final MapdevLogger mapdevLogger;
    private final Consumer<Optional<T>> callback;
    private final PathWatcherHandle handle;

    @Inject DocumentWatcher(@Assisted Path path, @Assisted Consumer<Optional<T>> callback, DocumentBuilder builder, DocumentParser<T> parser, MapdevLogger mapdevLogger, PathWatcherService watcherService, MainThreadExecutor executor) throws IOException {
        this.builder = builder;
        this.parser = parser;
        this.mapdevLogger = mapdevLogger;
        this.callback = callback;
        this.handle = watcherService.watch(path, executor, this);
    }

    public void cancel() {
        if(handle != null) {
            handle.cancel();
            callback.accept(Optional.empty());
        }
    }

    @Override
    public void fileCreated(Path path) {
        fileModified(path);
    }

    @Override
    public void fileModified(Path path) {
        final T document;
        try {
            document = parser.parse(builder.parse(path.toFile()));
        } catch(SAXException | IOException | ParseException e) {
            mapdevLogger.log(Level.SEVERE, "Failed to load " + path, e);
            return;
        }
        callback.accept(Optional.of(document));
    }

    @Override
    public void fileDeleted(Path path) {
        callback.accept(Optional.empty());
    }
}
