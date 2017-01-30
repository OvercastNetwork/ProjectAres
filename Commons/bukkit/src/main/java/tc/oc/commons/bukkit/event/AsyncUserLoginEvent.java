package tc.oc.commons.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import tc.oc.api.docs.User;
import tc.oc.api.users.LoginResponse;

/**
 * Fired from a background thread during login, after we have retrieved
 * the player's data and decided they are allowed to connect.
 *
 * There is NO guarantee that a synchronous login will follow for this
 * player. If the connection drops, or something else fails, before they
 * make it to the synchronous part of the login, then this event is the
 * last you will ever hear from them. For that reason, you need to be
 * careful about allocating any per-player resources from this event,
 * being sure to clean up any that leak from failed logins.
 */
public class AsyncUserLoginEvent extends Event implements UserEvent {

    private final LoginResponse response;

    public AsyncUserLoginEvent(LoginResponse response) {
        super(true);
        this.response = response;
    }

    public LoginResponse response() {
        return response;
    }

    @Override
    public User getUser() {
        return response.user();
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
