package tc.oc.commons.core.chat;

import tc.oc.minecraft.api.command.CommandSender;

/**
 * A factory to create {@link Audience}s for various purposes.
 *
 * Should be expanded to support other types.
 */
public interface Audiences<T extends CommandSender> {
    Audience get(T sender);

    Audience localServer();

    Audience withPermission(String permission);
}
