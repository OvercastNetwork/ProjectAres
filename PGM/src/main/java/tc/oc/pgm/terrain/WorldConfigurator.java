package tc.oc.pgm.terrain;

import org.bukkit.World;
import org.bukkit.WorldCreator;

/**
 * Something that configures {@link World}s when they are created,
 * through a {@link WorldCreator}.
 *
 * Use a {@link WorldConfiguratorBinder} to register these.
 */
public interface WorldConfigurator {
    void configureWorld(WorldCreator worldCreator);
}
