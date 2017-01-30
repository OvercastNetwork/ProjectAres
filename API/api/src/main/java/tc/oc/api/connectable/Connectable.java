package tc.oc.api.connectable;

import java.io.IOException;

import tc.oc.minecraft.api.event.Activatable;
import tc.oc.commons.core.plugin.PluginFacet;

/**
 * Service that needs to be connected and disconnected along with the API.
 *
 * Use a {@link ConnectableBinder} to register these.
 *
 * TODO: This should probably extend {@link PluginFacet},
 * but to do that, API needs to be able to find the services bound in other plugins.
 */
public interface Connectable extends Activatable {
    default void connect() throws IOException {};
    default void disconnect() throws IOException {};
}
