package tc.oc.commons.bukkit.event;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

/**
 * Extension of Bukkit's {@link Cancellable} to allow for custom error messages to be specified when cancelling the
 * event.
 */
public abstract class ExtendedCancellable extends Event implements Cancellable {

    protected boolean cancelled;
    protected @Nullable BaseComponent cancelMessage;

    protected ExtendedCancellable() {
        this(null);
    }

    protected ExtendedCancellable(@Nullable BaseComponent cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
        this.cancelMessage = null;
    }

    public void setCancelled(boolean cancel, BaseComponent message) {
        this.setCancelled(cancel);
        this.cancelMessage = message;
    }

    public @Nullable BaseComponent getCancelMessage() {
        return this.cancelMessage;
    }
}
