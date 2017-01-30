package tc.oc.commons.bukkit.trophies;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import tc.oc.api.docs.Trophy;
import tc.oc.api.docs.User;
import tc.oc.commons.bukkit.event.UserEvent;

/**
 * Called when a {@link Trophy} is either granted to or revoked from a {@link User}.
 */
public class TrophyEvent extends Event implements UserEvent {
    private static final HandlerList handlers = new HandlerList();

    private final User user;
    private final Trophy trophy;
    private final boolean grant;

    public TrophyEvent(User user, Trophy trophy, boolean grant) {
        this.user = user;
        this.trophy = trophy;
        this.grant = grant;
    }

    public Trophy getTrophy() {
        return trophy;
    }

    @Override
    public User getUser() {
        return user;
    }

    public boolean isGranting() {
        return grant;
    }

    public boolean isRevoking() {
        return !grant;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
