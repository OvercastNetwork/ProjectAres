package tc.oc.pgm.map;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.development.MapErrorTracker;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class MapLoaderImpl implements MapLoader {

    protected final Logger logger;
    protected final Path serverRoot;
    protected final MapConfiguration config;
    protected final PGMMap.Factory mapFactory;
    protected final MapErrorTracker mapErrorTracker;

    @Inject MapLoaderImpl(Loggers loggers, @Named("serverRoot") Path serverRoot, MapConfiguration config, PGMMap.Factory mapFactory, MapErrorTracker mapErrorTracker) {
        this.mapErrorTracker = mapErrorTracker;
        this.logger = loggers.get(getClass());
        this.serverRoot = serverRoot;
        this.config = checkNotNull(config);
        this.mapFactory = mapFactory;
    }

    @Override
    public boolean loadMap(PGMMap map) throws MapNotFoundException {
        mapErrorTracker.clearErrors(map);
        return map.reload();
    }

    @Override
    public List<PGMMap> loadNewMaps(Map<Path, PGMMap> loaded, Set<Path> added, Set<Path> updated, Set<Path> removed) {
        checkArgument(added.isEmpty());
        checkArgument(removed.isEmpty());

        logger.fine("Loading maps...");

        Set<Path> found = new HashSet<>();
        List<PGMMap> maps = new ArrayList<>();

        for(MapSource source : config.sources()) {
            try {
                for(Path path : source.getMapFolders(logger)) {
                    try {
                        found.add(path);
                        PGMMap map = loaded.get(path);
                        if(map == null) {
                            logger.fine("  ADDED " + path);
                            added.add(path);

                            map = mapFactory.create(new MapFolder(source, path));
                            if(loadMap(map)) maps.add(map);
                        } else if(map.shouldReload()) {
                            logger.fine("  UPDATED " + path);
                            updated.add(path);
                            loadMap(map);
                        }
                    } catch(MapNotFoundException e) {
                        // ignore - will be removed below
                    }
                }
            } catch(IOException e) {
                logger.log(Level.SEVERE, "Exception loading from map source " + source.getPath(), e);
            }
        }

        for(Path path : loaded.keySet()) {
            if(!found.contains(path)) {
                logger.fine("  REMOVED " + path);
                removed.add(path);
            }
        }

        logger.fine("Found " + found.size() + " maps, " + added.size() + " new, " + removed.size() + " removed");
        return maps;
    }
}
