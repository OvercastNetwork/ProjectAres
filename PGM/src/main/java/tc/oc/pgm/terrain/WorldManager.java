package tc.oc.pgm.terrain;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;

import org.bukkit.World;
import tc.oc.pgm.module.ModuleLoadException;

/**
 * Creator and destroyer of {@link World}s
 */
public interface WorldManager {

    /**
     * Create or load a world based on the given settings.
     * If the world is already loaded, it will be returned.
     */
    World createWorld(String worldName) throws ModuleLoadException, IOException;

    /**
     * Unload the given world
     */
    void unloadWorld(World world);

    /**
     * Destroy the given world, optionally copying it to the given archive location first.
     */
    void destroyWorld(String worldName, @Nullable File archive);
}
