package tc.oc.pgm.rotation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.time.Instant;

import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.commons.core.logging.ClassLogger;
import tc.oc.commons.core.util.Joiners;
import tc.oc.pgm.PGM;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.PGMMap;

public class FileRotationProvider extends AbstractRotationProvider {
    public static final String FILE_NEXTID_SUFFIX = ".next";
    public static final int DEFAULT_NEXTID = 0;

    private final Logger logger;
    private final Logger mapLogger;
    private final MapLibrary mapLibrary;
    private final String name;
    private final Path rotationFile;
    private final Path dataPath;
    private final Optional<ServerDoc.Rotation> rotationApi;

    public FileRotationProvider(MapLibrary mapLibrary, String name, Path rotationFile, Path dataPath, Optional<ServerDoc.Rotation> rotationApi) {
        Preconditions.checkNotNull(mapLibrary, "map manager");
        Preconditions.checkNotNull(rotationFile, "rotation path");
        Preconditions.checkNotNull(dataPath, "data path");
        Preconditions.checkArgument(Files.isRegularFile(rotationFile), "rotation path must be a file");
        Preconditions.checkArgument(Files.isDirectory(dataPath), "data path must be a directory");

        this.logger = ClassLogger.get(PGM.get().getLogger(), getClass());
        this.mapLogger = PGM.get().getRootMapLogger();
        this.mapLibrary = mapLibrary;
        this.name = name;
        this.rotationFile = rotationFile;
        this.dataPath = dataPath;
        this.rotationApi = rotationApi;
    }

    Path nextIdFile() {
        return dataPath.resolve(name + FILE_NEXTID_SUFFIX);
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
        int nextId = this.parseNextId(maps);

        if(maps.isEmpty()) {
            throw new IOException(String.format("Rotation '%s' was empty!", name));
        }

        if(!RotationState.isNextIdValid(maps, nextId)) {
            nextId = 0;
        }

        logger.info("Loaded rotation '" + name + "' with " + maps.size() + " maps: " + Joiners.onCommaSpace.join(maps));

        return new RotationState(maps, nextId);
    }

    private int parseNextId(List<PGMMap> maps) {
        List<String> lines;
        try {
            lines = Files.readAllLines(nextIdFile(), Charsets.UTF_8);
        } catch (IOException e) {
            return rotationApi.map(rot -> maps.indexOf(mapLibrary.getMapByNameOrId(rot.next_map_id()).get()))
                              .flatMap(index -> Optional.ofNullable(index >= 0 ? index : null))
                              .orElseGet(() -> {
                this.logger.warning("Failed to read next id from " + nextIdFile().toString());
                return DEFAULT_NEXTID;
            });
        }

        for(String line : lines) {
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                continue;
            }
        }

        this.logger.warning("Failed to parse next id from " + nextIdFile().toString());
        return DEFAULT_NEXTID;
    }

    private List<PGMMap> parseRotationNames() throws IOException {
        List<String> lines = Files.readAllLines(rotationFile, Charsets.UTF_8);

        ImmutableList.Builder<PGMMap> maps = ImmutableList.builder();
        for(String line : lines) {
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

        return maps.build();
    }

    @Override
    public Future<?> saveRotation(@Nonnull final String name, @Nonnull final RotationState rotation) {
        this.setRotation(name, rotation);
        return getExecutorService().submit(() -> saveRotationToDisk(name, rotation));
    }

    private void saveRotationToDisk(@Nonnull String name, @Nonnull RotationState rotation) {
        List<String> nextIdSerialized = ImmutableList.of(Integer.toString(rotation.getNextId()));
        try {
            Files.write(nextIdFile(), nextIdSerialized, Charsets.UTF_8);
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Failed to save next id for rotation: " + name, e);
        }
    }

    private static @Nonnull ExecutorService getExecutorService() {
        if(fileExecutorService == null) {
            fileExecutorService = Executors.newSingleThreadExecutor();
        }
        return fileExecutorService;
    }
    private static ExecutorService fileExecutorService;
}
