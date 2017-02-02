package tc.oc.pgm.map;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.message.types.UpdateMultiResponse;

public interface MapLibrary {

    Set<PGMMap> addMaps(Collection<PGMMap> maps);

    boolean addMap(PGMMap map);

    void removeMaps(Collection<Path> paths);

    boolean removeMap(Path path);

    Logger getLogger();

    Collection<PGMMap> getMaps();

    Set<String> getMapNames();

    Map<Path, PGMMap> getMapsByPath();

    @Nullable PGMMap getMapById(String mapId);

    @Nullable PGMMap getMapById(MapId mapId);

    PGMMap needMapById(String mapId);

    PGMMap needMapById(MapId mapId);

    Optional<PGMMap> getMapByNameOrId(String nameOrId);

    List<PGMMap> resolveMaps(List<String> namesOrIds);

    Collection<PGMMap> getDirtyMaps();

    ListenableFuture<UpdateMultiResponse> pushAllMaps();

    ListenableFuture<UpdateMultiResponse> pushDirtyMaps();
}
