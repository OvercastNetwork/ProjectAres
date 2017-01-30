package tc.oc.pgm.map;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface MapConfiguration {
    List<Path> includePaths();
    List<Path> globalIncludes();
    Map<String, Boolean> environment();
    boolean autoReload();
    boolean reloadWhenError();
    List<MapSource> sources();
}
