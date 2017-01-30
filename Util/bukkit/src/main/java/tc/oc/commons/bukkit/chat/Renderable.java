package tc.oc.commons.bukkit.chat;

import org.bukkit.command.CommandSender;

/**
 * Generates {@link T}s for specific {@link CommandSender} viewers
 */
public interface Renderable<T> {

    T render(ComponentRenderContext context, CommandSender viewer);

    static <U> Renderable<U> of(U u) {
        return (context, viewer) -> u;
    }
}
