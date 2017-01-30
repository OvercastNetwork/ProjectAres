package tc.oc.file;

import java.nio.file.Path;

public interface PathWatcher {

    default void fileCreated(Path path) {}

    default void fileModified(Path path) {}

    default void fileDeleted(Path path) {}
}
