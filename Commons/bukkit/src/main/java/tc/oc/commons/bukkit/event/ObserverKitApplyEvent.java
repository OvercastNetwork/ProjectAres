package tc.oc.commons.bukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EntityAction;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a non-participating player spawns (observers in PGM, everybody in the Lobby)
 */
public class ObserverKitApplyEvent extends PlayerEvent implements EntityAction {
    public ObserverKitApplyEvent(Player player) {
        super(player);
    }

    @Override
    public Player getActor() {
        return getPlayer();
    }

    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
