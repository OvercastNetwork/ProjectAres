package tc.oc.commons.bukkit.event;

import tc.oc.api.docs.User;

/**
 * Represents an event involving a {@link User}.
 */
public interface UserEvent {
    User getUser();
}
