package tc.oc.pgm.terrain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import tc.oc.commons.core.FileUtils;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.module.ModuleLoadException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Provides handy methods to load, save, and archive match worlds.
 */
public class WorldManagerImpl implements WorldManager {

    private final Server server;
    private final MapInfo mapInfo;
    private final TerrainOptions terrainOptions;
    private final Set<WorldConfigurator> worldConfigurators;

    @Inject private WorldManagerImpl(Server server, MapInfo mapInfo, TerrainOptions terrainOptions, Set<WorldConfigurator> worldConfigurators) {
        this.server = server;
        this.mapInfo = mapInfo;
        this.terrainOptions = terrainOptions;
        this.worldConfigurators = worldConfigurators;
    }

    private File worldFolder(String worldName) {
        return new File(server.getWorldContainer(), worldName);
    }

    private void destroy(String worldName) throws IOException {
        final File folder = worldFolder(worldName);
        if(folder.exists()) {
            FileUtils.delete(folder);
        }
    }

    private void copyDirs(File source, File dest, String... names) throws IOException {
        for(String name : names) {
            final File dir = new File(source, name);
            if(dir.isDirectory()) {
                FileUtils.copy(dir, new File(dest, name));
            }
        }
    }

    private void copy(File source, String worldName) throws IOException {
        final File folder = worldFolder(worldName);

        // only copy level.dat, region/, and data/
        if(!folder.mkdir()) {
            throw new IOException("Failed to create temporary world folder " + folder);
        }

        FileUtils.copy(new File(source, "level.dat"), new File(folder, "level.dat"));
        copyDirs(source, folder, "region", "data", "structures");
    }

    private void importDestructive(File source, String worldName) throws IOException {
        destroy(worldName);
        copy(source, worldName);
    }

    private WorldCreator worldCreator(String worldName) {
        final WorldCreator creator = server.detectWorld(worldName);
        return creator != null ? creator : new WorldCreator(worldName);
    }

    @Override
    public World createWorld(String worldName) throws ModuleLoadException, IOException {
        if(server.getWorlds().isEmpty()) {
            throw new IllegalStateException("Can't create a world because there is no default world to derive it from");
        }

        try {
            importDestructive(terrainOptions.worldFolder().toFile(), worldName);
        } catch(FileNotFoundException e) {
            // If files are missing, just inform the mapmaker.
            // Other IOExceptions are considered internal errors.
            throw new ModuleLoadException(e.getMessage()); // Don't set the cause, it's redundant
        }

        final WorldCreator creator = worldCreator(worldName);
        worldConfigurators.forEach(wc -> wc.configureWorld(creator));

        final World world = server.createWorld(creator);
        if(world == null) {
            throw new IllegalStateException("Failed to create world (Server.createWorld returned null)");
        }

        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        world.setDifficulty(Optional.ofNullable(mapInfo.difficulty)
                                    .orElseGet(() -> server.getWorlds().get(0).getDifficulty()));

        return world;
    }

    @Override
    public void unloadWorld(World world) {
        this.server.unloadWorld(world, true);
    }

    @Override
    public void destroyWorld(String worldName, @Nullable File archive) {
        File folder = worldFolder(worldName);
        if(!folder.exists()) return;

        if(archive != null) {
            cleanWorldDirectory(folder);
            checkState(folder.renameTo(archive));
        } else {
            FileUtils.delete(folder);
        }
    }

    /**
     * Cleans the world directory of generated files.  Currently it deletes:
     * - session.lock
     * - uid.dat
     *
     * @param dir File pointing to the world directory.
     */
    private void cleanWorldDirectory(File dir) {
        new File(dir, "session.lock").delete();
        new File(dir, "uid.dat").delete();
    }
}
