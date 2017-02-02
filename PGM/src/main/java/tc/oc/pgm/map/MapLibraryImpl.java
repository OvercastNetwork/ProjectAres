package tc.oc.pgm.map;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import tc.oc.api.docs.virtual.UserDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.maps.MapService;
import tc.oc.api.maps.UpdateMapsResponse;
import tc.oc.api.message.types.UpdateMultiResponse;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.commons.core.util.SystemFutureCallback;
import tc.oc.minecraft.scheduler.SyncExecutor;

@Singleton
public class MapLibraryImpl implements MapLibrary {

    private final SyncExecutor syncExecutor;
    private final MapService mapService;

    protected final Map<MapId, PGMMap> mapsById = Maps.newHashMap();
    protected final Map<Path, PGMMap> mapsByPath = new HashMap<>();
    protected final SetMultimap<String, PGMMap> mapsByName = HashMultimap.create();
    protected final Logger logger;

    @Inject MapLibraryImpl(Loggers loggers, SyncExecutor syncExecutor, MapService mapService) {
        this.syncExecutor = syncExecutor;
        this.mapService = mapService;
        this.logger = loggers.get(getClass());
    }

    @Override
    public Set<PGMMap> addMaps(Collection<PGMMap> maps) {
        Set<PGMMap> added = new HashSet<>();
        for(PGMMap map : maps) {
            if(addMap(map)) added.add(map);
        }
        return added;
    }

    @Override
    public boolean addMap(PGMMap map) {
        final MapId id = map.getId();
        PGMMap old = mapsById.get(id);

        if(old == null) {
            logger.fine("Adding " + id);
        } else if(old.getSource().hasPriorityOver(map.getSource())) {
            logger.fine("Skipping duplicate " + id);
            return false;
        } else {
            logger.fine("Replacing duplicate " + id);
        }

        mapsById.put(id, map);
        mapsByPath.put(map.getFolder().getAbsolutePath(), map);
        mapsByName.put(map.getName(), map);
        return true;
    }

    @Override
    public void removeMaps(Collection<Path> paths) {
        for(Path path : paths) removeMap(path);
    }

    @Override
    public boolean removeMap(Path path) {
        PGMMap map = mapsByPath.remove(path);
        if(map == null) return false;

        mapsById.remove(map.getId());
        mapsByName.remove(map.getName(), map);
        return true;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Collection<PGMMap> getMaps() {
        return this.mapsById.values();
    }

    @Override
    public Set<String> getMapNames() {
        return mapsByName.keySet();
    }

    @Override
    public Map<Path, PGMMap> getMapsByPath() {
        return mapsByPath;
    }

    @Override
    public @Nullable PGMMap getMapById(String mapId) {
        return getMapById(MapId.parse(mapId));
    }

    @Override
    public @Nullable PGMMap getMapById(MapId mapId) {
        return mapsById.get(mapId);
    }

    @Override
    public PGMMap needMapById(String mapId) {
        return needMapById(MapId.parse(mapId));
    }

    @Override
    public PGMMap needMapById(MapId mapId) {
        final PGMMap map = getMapById(mapId);
        if(map == null) {
            throw new IllegalStateException("No map with ID '" + mapId + "'");
        }
        return map;
    }

    @Override
    public Optional<PGMMap> getMapByNameOrId(String nameOrId) {
        Set<PGMMap> maps = mapsByName.get(nameOrId);

        if(maps.isEmpty()) {
            return Optional.ofNullable(mapsById.get(MapId.parse(nameOrId)));
        }

        PGMMap best = null;
        for(PGMMap map : maps) {
            if(best == null || map.getSource().hasPriorityOver(best.getSource())) {
                best = map;
            }
        }

        return Optional.of(best);
    }

    @Override
    public List<PGMMap> resolveMaps(List<String> namesOrIds) {
        List<PGMMap> mapResult = Lists.newArrayList();
        for(String slug : namesOrIds) {
            Optional<PGMMap> map = this.getMapByNameOrId(slug);
            if(map.isPresent()) {
                mapResult.add(map.get());
            } else {
                this.logger.warning("Could not find map: " + slug);
            }
        }
        return mapResult;
    }

    @Override
    public Collection<PGMMap> getDirtyMaps() {
        return Collections2.filter(getMaps(), (map) -> !map.isPushed());
    }

    @Override
    public ListenableFuture<UpdateMultiResponse> pushAllMaps() {
        return pushMaps(getMaps());
    }

    @Override
    public ListenableFuture<UpdateMultiResponse> pushDirtyMaps() {
        return pushMaps(getDirtyMaps());
    }

    private ListenableFuture<UpdateMultiResponse> pushMaps(final Collection<PGMMap> maps) {
        final Set<PGMMap> pushedMaps = ImmutableSet.copyOf(maps);

        final SetMultimap<UUID, PGMMap> mapsByContributorId = maps.stream().collect(Collectors.indexingByMulti(
            map -> map.getInfo()
                      .allContributors()
                      .map(Contributor::getUuid)
                      .filter(Objects::nonNull)
        ));

        logger.info("Pushing " + pushedMaps.size() +
                    " maps and resolving " + mapsByContributorId.keySet().size() + " contributor names");

        final UpdateMapsResponse response = mapService.updateMaps(
            Collections2.transform(pushedMaps, PGMMap::getDocument)
        );

        response.authors().forEach(
            (uuid, future) -> syncExecutor.callback(
                future,
                SystemFutureCallback.<UserDoc.Identity>onSuccess(
                    user -> {
                        logger.fine(() -> "Resolved map author " + uuid + " to " + user.username());
                        mapsByContributorId.removeAll(uuid).forEach(
                            map -> map.getInfo().allContributors().forEach(contributor -> {
                                if(uuid.equals(contributor.getUuid())) {
                                    contributor.setUser(user);
                                }
                            })
                        );
                    }
                ).onFailure(NotFound.class, ex ->
                    mapsByContributorId.removeAll(uuid).forEach(
                        map -> map.getLogger().severe("Contributor UUID not found: " + uuid)
                    )
                ).onCompletion(() -> {
                    if(mapsByContributorId.isEmpty()) {
                        logger.info("Finished resolving map contributors");
                    }
                })
            )
        );

        final SettableFuture<UpdateMultiResponse> future = SettableFuture.create();
        syncExecutor.callback(
            response.maps(),
            SystemFutureCallback.<UpdateMultiResponse>onSuccess(mapResponse -> {
                logger.info("Push complete: " + mapResponse);
                logErrors(mapResponse);
                pushedMaps.forEach(PGMMap::markPushed);
                future.set(mapResponse);
            }).onFailure(Throwable.class, future::setException)
        );

        return future;
    }

    private void logErrors(UpdateMultiResponse response) {
        for(Map.Entry<String, Map<String, List<String>>> mapEntry : response.errors.entrySet()) {
            final PGMMap map = needMapById(mapEntry.getKey());
            for(Map.Entry<String, List<String>> propEntry : mapEntry.getValue().entrySet()) {
                for(String message : propEntry.getValue()) {
                    map.getLogger().severe("Error saving map to database: " + propEntry.getKey() + " " + message);
                }
            }
        }
    }
}
