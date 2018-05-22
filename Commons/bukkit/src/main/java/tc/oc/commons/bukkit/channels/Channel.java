package tc.oc.commons.bukkit.channels;

import org.bukkit.command.CommandSender;
import tc.oc.api.docs.Chat;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.virtual.ChatDoc;
import tc.oc.commons.core.chat.Audience;

import javax.annotation.Nullable;

/**
 * An {@link Audience} that sends {@link Chat} messages to the API.
 */
public interface Channel extends Audience {

    ChatDoc.Type type();

    void chat(CommandSender sender, String message);

    void chat(@Nullable PlayerId playerId, String message);

    void show(Chat message);

    boolean sendable(CommandSender sender);

    boolean viewable(CommandSender sender);

}
