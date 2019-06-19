package tc.oc.pgm.rotation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.time.Instant;

import com.google.common.util.concurrent.Futures;
import org.bukkit.configuration.file.YamlConfiguration;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.logging.ClassLogger;
import tc.oc.commons.core.util.Joiners;
import tc.oc.pgm.PGM;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.PGMMap;

public class FileRotationProvider extends AbstractRotationProvider {
    private final Logger logger;
    private final Logger mapLogger;
    private final MapLibrary mapLibrary;
    private final String name;
    private final Path rotationFile;
    private final Optional<ServerDoc.Rotation> rotationApi;
    private final boolean shuffle;

    public FileRotationProvider(MapLibrary mapLibrary, String name, Path rotationFile, Optional<ServerDoc.Rotation> rotationApi, boolean shuffle) {
        Preconditions.checkNotNull(mapLibrary, "map manager");
        Preconditions.checkNotNull(rotationFile, "rotation path");
        Preconditions.checkArgument(Files.isRegularFile(rotationFile), "rotation path must be a file");

        this.logger = ClassLogger.get(PGM.get().getLogger(), getClass());
        this.mapLogger = PGM.get().getRootMapLogger();
        this.mapLibrary = mapLibrary;
        this.name = name;
        this.rotationFile = rotationFile;
        this.rotationApi = rotationApi;
        this.shuffle = shuffle;
    }

    @Override
    public @Nonnull Future<?> loadRotations() {
        return getExecutorService().submit(() -> {
            try {
                setRotation(name, loadRotationFromDisk(), Instant.now());
            } catch(IOException e) {
                mapLogger.log(Level.SEVERE, "Failed to load file rotation", e);
            }
        });
    }

    private RotationState loadRotationFromDisk() throws IOException {
        List<PGMMap> maps = this.parseRotationNames();
        int nextId = this.fetchNextId(maps);

        if(maps.isEmpty()) {
            throw new IOException(String.format("Rotation '%s' was empty!", name));
        }

        if(!RotationState.isNextIdValid(maps, nextId)) {
            nextId = 0;
        }

        logger.info("Loaded rotation '" + name + "' with " + maps.size() + " maps: " + Joiners.onCommaSpace.join(maps));

        return new RotationState(maps, nextId);
    }

    // TODO: Provide an alternative implementation for no API
    private int fetchNextId(List<PGMMap> maps) {
        return rotationApi.map(rot -> maps.indexOf(mapLibrary.getMapByNameOrId(rot.next_map_id()).get()))
            .flatMap(index -> Optional.ofNullable(index >= 0 ? index : null))
            .orElse(0);
    }

    private List<PGMMap> parseRotationNames() throws IOException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(rotationFile.toFile());
        List<PGMMap> maps = new ArrayList<>();
        for(String line : yaml.getStringList("maps")) {
            line = line.trim();
            if(line.isEmpty()) {
                continue;
            }

            Optional<PGMMap> map = this.mapLibrary.getMapByNameOrId(line);
            if(map.isPresent()) {
                maps.add(map.get());
            } else {
                mapLogger.severe("Unknown map '" + line + "' when parsing " + rotationFile.toString());
            }
        }
        if(shuffle) Collections.shuffle(maps);
        return ImmutableList.copyOf(maps);
    }

    @Override
    public Future<?> saveRotation(@Nonnull final String name, @Nonnull final RotationState rotation) {
        this.setRotation(name, rotation);
        return Futures.immediateFuture(null);
    }

    private static @Nonnull ExecutorService getExecutorService() {
        if(fileExecutorService == null) {
            fileExecutorService = Executors.newSingleThreadExecutor();
        }
        return fileExecutorService;
    }
    private static ExecutorService fileExecutorService;
}
