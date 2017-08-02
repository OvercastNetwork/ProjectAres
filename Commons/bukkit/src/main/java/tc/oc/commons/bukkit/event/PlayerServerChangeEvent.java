package tc.oc.commons.bukkit.event;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.PlayerAction;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerServerChangeEvent extends ExtendedCancellable implements PlayerAction {

    private final Player player;
    private final String datacenter;
    private final @Nullable String bungeeName;

    public PlayerServerChangeEvent(Player player, String datacenter, @Nullable String bungeeName, BaseComponent cancelMessage) {
        super(cancelMessage);
        this.datacenter = datacenter;
        this.player = checkNotNull(player);
        this.bungeeName = bungeeName;
    }

    @Override
    public Player getActor() {
        return player;
    }

    public Player getPlayer() {
        return player;
    }

    public @Nullable String getBungeeName() {
        return bungeeName;
    }

    @Override public HandlerList getHandlers() { return handlers; }
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }
}
