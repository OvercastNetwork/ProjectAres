package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

/**
 * Performs server-side transformations on components to prepare them for sending to the client
 */
public interface ComponentRenderer<T extends BaseComponent> {

    /**
     * Transform the given component tree into one that can be sent to the given viewer's client,
     * i.e. is made entirely of components that the client knows how to render. If no transformations
     * are required, the original component tree should be returned.
     * @param context     rendering context that can be used to render child components
     * @param original    original component tree
     * @param viewer      viewer of the rendered component
     * @return            rendered component
     */
    BaseComponent render(ComponentRenderContext context, T original, CommandSender viewer);
}
