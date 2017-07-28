package tc.oc.pgm.match;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventBus;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.api.util.Permissions;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.pgm.PGM;
import tc.oc.pgm.development.MapErrorTracker;
import tc.oc.pgm.events.SetNextMapEvent;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.MapLoader;
import tc.oc.pgm.map.MapNotFoundException;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.rotation.FileRotationProviderFactory;
import tc.oc.pgm.rotation.RotationManager;
import tc.oc.pgm.rotation.RotationState;

@Singleton
public class MatchManager implements MatchFinder {

    // Maximum randomly selected maps to attempt to load from the library before giving up
    private static final int MAX_CYCLE_TRIES = 10;

    private final Logger log;
    private final Path pluginDataFolder;
    private final Provider<Configuration> config;
    private final MapLibrary mapLibrary;
    private final MapLoader mapLoader;
    private final MapErrorTracker mapErrorTracker;
    private final FileRotationProviderFactory fileRotationProviderFactory;
    private final EventBus eventBus;
    private final MatchLoader matchLoader;
    private final MinecraftService minecraftService;
    private @Nullable RotationManager rotationManager;

    /** Custom set next map. */
    private PGMMap nextMap = null;

    /**
     * Creates a new map manager with a specified map rotation.
     */
    @Inject MatchManager(Loggers loggers,
                         @Named("pluginData") Path pluginDataFolder,
                         Provider<Configuration> config,
                         MapLibrary mapLibrary,
                         MapLoader mapLoader,
                         MapErrorTracker mapErrorTracker,
                         FileRotationProviderFactory fileRotationProviderFactory,
                         EventBus eventBus,
                         MatchLoader matchLoader,
                         MinecraftService minecraftService) throws MapNotFoundException {

        this.pluginDataFolder = pluginDataFolder;
        this.mapErrorTracker = mapErrorTracker;
        this.fileRotationProviderFactory = fileRotationProviderFactory;
        this.log = loggers.get(getClass());
        this.config = config;
        this.mapLibrary = mapLibrary;
        this.mapLoader = mapLoader;
        this.eventBus = eventBus;
        this.matchLoader = matchLoader;
        this.minecraftService = minecraftService;
    }

    @Override
    public Map<World, Match> matchesByWorld() {
        return matchLoader.matchesByWorld();
    }

    /** Gets the currently loaded maps. */
    public Collection<PGMMap> getMaps() {
        return this.mapLibrary.getMaps();
    }

    public Set<PGMMap> loadNewMaps() throws MapNotFoundException {
        log.info("Loading maps...");

        Set<Path> added = new HashSet<>(),
                  updated = new HashSet<>(),
                  removed = new HashSet<>();
        List<PGMMap> maps = mapLoader.loadNewMaps(mapLibrary.getMapsByPath(), added, updated, removed);
        mapLibrary.removeMaps(removed);
        Set<PGMMap> newMaps = mapLibrary.addMaps(maps);
        mapLibrary.pushDirtyMaps();

        log.info("Loaded " + newMaps.size() + " maps");

        if(mapLibrary.getMaps().isEmpty()) {
            throw new MapNotFoundException();
        }

        return newMaps;
    }

    public boolean loadRotations() {
        return getRotationManager().load(mapLibrary.getMaps().iterator().next());
    }

    public Set<PGMMap> loadMapsAndRotations() throws MapNotFoundException {
        Set<PGMMap> maps = loadNewMaps();
        loadRotations();
        PGM.getPollableMaps().loadPollableMaps();
        return maps;
    }

    public Path getPluginDataFolder() {
        return pluginDataFolder;
    }

    public RotationManager getRotationManager() {
        if(rotationManager == null) {
            rotationManager = new RotationManager(
                log,
                minecraftService,
                config.get(),
                mapLibrary.getMaps().iterator().next(),
                fileRotationProviderFactory.parse(
                    mapLibrary,
                    pluginDataFolder,
                    config.get()
                )
            );
        }
        return this.rotationManager;
    }

    /**
     * Gets the next map that will be loaded at this point in time.  If a map
     * had been specified explicitly (setNextMap) that will be returned,
     * otherwise the next map in the rotation will be returned.
     *
     * @return Next map that would be loaded at this point in time.
     */
    public PGMMap getNextMap() {
        if(this.nextMap == null) {
            return this.getRotationManager().getRotation().getNext();
        } else {
            return this.nextMap;
        }
    }

    /**
     * @return the number of cycles before the rotation starts repeating
     */
    public int cyclesBeforeRepeat() {
        int size = getRotationManager().getRotation().getMaps().size();
        if(nextMap != null) size++; // Extra map is available due to /setnext
        return size;
    }

    private PGMMap advanceRotation() {
        PGMMap currentMap;
        if(nextMap == null) {
            RotationState rotation = getRotationManager().getRotation();
            currentMap = rotation.getNext();
            rotation = rotation.skip(1);
            getRotationManager().setRotation(rotation);
        } else {
            currentMap = nextMap;
            this.nextMap = null;
        }

        eventBus.callEvent(new SetNextMapEvent(getNextMap()));
        return currentMap;
    }

    /**
     * Specified an explicit map for the next cycle.
     *
     * @param map to be loaded next.
     */
    public void setNextMap(PGMMap map) {
        if(map != nextMap) {
            this.nextMap = map;
            eventBus.callEvent(new SetNextMapEvent(map));
        }
    }

    /**
     * Cycle to the next map in the rotation
     * @param oldMatch          The current match, if any
     * @param retryRotation     Try every map in the rotation until one loads successfully
     * @param retryLibrary      Try every map in the library, after trying the entire rotation
     * @return                  The new match, or null if no map could be loaded
     */
    public @Nullable Match cycleToNext(@Nullable Match oldMatch, boolean retryRotation, boolean retryLibrary) {
        // Match unload also does this, but doing it earlier avoids some problems.
        // Specifically, RestartCountdown cannot cancel itself during a cycle.
        if(oldMatch != null) {
            oldMatch.countdowns().cancelAll();
        }

        Set<PGMMap> failed = new HashSet<>(); // Don't try any map more than once

        // Try to load a rotation map
        int maxCycles = cyclesBeforeRepeat();
        for(int cycles = 0; cycles < maxCycles; cycles++) {
            PGMMap map = advanceRotation();

            if(!failed.contains(map)) {
                Match match = cycleTo(oldMatch, map);
                if(match != null) return match;
            }

            // If retryRotation is false, give up after the first failure
            if(!retryRotation) return null;

            failed.add(map);
        }

        // If all rotation maps failed, and we're not allowed to try non-rotation maps, give up
        if(!retryLibrary) return null;

        // Try every map in the library, in random order, to avoid getting stuck in a folder full of broken maps
        final List<PGMMap> maps = new ArrayList<>(mapLibrary.getMaps());
        maps.removeAll(failed);
        Collections.shuffle(maps);

        int tries = 0;
        for(PGMMap map : maps) {
            if(++tries >= MAX_CYCLE_TRIES) return null;
            Match match = cycleTo(oldMatch, map);
            if(match != null) return match;
            failed.add(map);
        }

        return null;
    }

    public boolean hasMapSet() {
        return this.nextMap != null;
    }

    private @Nullable Match cycleTo(@Nullable Match oldMatch, PGMMap map) {
        try {
            mapErrorTracker.clearErrors(map);

            if(map.shouldReload()) {
                Bukkit.broadcast(ChatColor.GREEN + "XML changes detected, reloading", Permissions.MAPERRORS);
                mapLoader.loadMap(map);
                mapLibrary.pushDirtyMaps();
            }

            return matchLoader.cycleTo(oldMatch, map);
        } catch(MapNotFoundException e) {
            // Maps are sometimes removed, must handle it gracefully
            log.warning("Skipping deleted map " + map.getName());
            try {
                loadMapsAndRotations();
            } catch(MapNotFoundException e2) {
                log.severe("No maps could be loaded, server cannot cycle");
            }
            return null;
        }
    }
}
