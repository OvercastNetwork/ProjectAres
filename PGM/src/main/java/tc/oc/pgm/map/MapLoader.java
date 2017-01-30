package tc.oc.pgm.map;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MapLoader {

    List<PGMMap> loadNewMaps(Map<Path, PGMMap> loaded, Set<Path> added, Set<Path> updated, Set<Path> removed);

    /**
     * Load/reload the given map
     * @throws MapNotFoundException if the map was not found at its source
     */
    boolean loadMap(PGMMap map) throws MapNotFoundException;
}
