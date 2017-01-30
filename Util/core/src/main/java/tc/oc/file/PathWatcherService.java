package tc.oc.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executor;

public interface PathWatcherService {
    PathWatcherHandle watch(Path path, Executor executor, PathWatcher callback) throws IOException;
}
