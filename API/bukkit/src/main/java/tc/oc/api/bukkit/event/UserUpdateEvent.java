package tc.oc.api.bukkit.event;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import tc.oc.api.docs.User;

public class UserUpdateEvent extends PlayerEvent {

    private final @Nullable User before;
    private final @Nullable User after;

    public UserUpdateEvent(Player player, @Nullable User before, @Nullable User after) {
        super(player);
        this.before = before;
        this.after = after;
    }

    public @Nullable User before() {
        return before;
    }

    public @Nullable User after() {
        return after;
    }

    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
    private static final HandlerList handlers = new HandlerList();
}
