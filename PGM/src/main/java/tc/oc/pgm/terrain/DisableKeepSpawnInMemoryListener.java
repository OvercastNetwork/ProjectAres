package tc.oc.pgm.terrain;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * This prevents the server from pre-loading 24x24 chunks around spawn,
 * which would otherwise happen on the main thread and probably
 * disconnect everyone if terrain needs to be generated.
 *
 * There's no better place to do this, because this is the only plugin
 * code that can run between the world being created and the spawn chunks
 * being generated.
 */
public class DisableKeepSpawnInMemoryListener implements PluginFacet, Listener {
    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        event.getWorld().setKeepSpawnInMemory(false);
    }
}
