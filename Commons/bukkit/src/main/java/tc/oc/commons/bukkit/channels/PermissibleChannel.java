package tc.oc.commons.bukkit.channels;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import tc.oc.commons.core.chat.Audience;

/**
 * An {@link Audience} with membership access based off of a {@link Permission} node.
 */
public interface PermissibleChannel extends Channel {

    Permission permission();

    @Override
    default boolean sendable(CommandSender sender) {
        return sender.hasPermission(permission());
    }

    @Override
    default boolean viewable(CommandSender sender) {
        return sender.hasPermission(permission());
    }

}

