package tc.oc.commons.bukkit.channels;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import tc.oc.api.docs.PlayerId;

import javax.annotation.Nullable;

/**
 * Called when a command sender chats in a local {@link Channel}.
 * If cancelled, the message will not be seen by users or reported to the API.
 */
public class ChannelChatEvent extends Event implements Cancellable {
    private final static HandlerList handlers = new HandlerList();

    private final Channel channel;
    private final PlayerId sender;
    private final String message;
    private boolean cancelled;

    public ChannelChatEvent(Channel channel, PlayerId sender, String message) {
        this.channel = channel;
        this.sender = sender;
        this.message = message;
    }

    public Channel channel() {
        return channel;
    }

    public @Nullable
    PlayerId sender() {
        return sender;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
