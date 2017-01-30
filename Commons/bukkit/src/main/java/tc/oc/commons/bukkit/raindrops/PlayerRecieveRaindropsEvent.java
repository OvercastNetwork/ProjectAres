package tc.oc.commons.bukkit.raindrops;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerRecieveRaindropsEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    protected final int raindrops;
    protected final int multiplier;
    protected final BaseComponent reason;

    public PlayerRecieveRaindropsEvent(Player who, int raindrops, int multiplier, BaseComponent reason) {
        super(who);
        this.raindrops = raindrops;
        this.multiplier = multiplier;
        this.reason = reason;
    }

    public int getRaindrops() {
        return this.raindrops;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public BaseComponent getReason() {
        return this.reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
