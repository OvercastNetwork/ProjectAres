package tc.oc.commons.bukkit.event;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerLoginEvent;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.User;
import tc.oc.api.users.LoginResponse;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fired from within {@link PlayerLoginEvent}, and inludes our {@link User} document.
 * It also fires after various login info has been loaded e.g. PlayerId, permissions,
 * settings, friends.
 *
 * It's a good idea to use this event instead of {@link PlayerLoginEvent} whenever
 * possible, to maximize the amount of valid state loaded for the player.
 *
 * Choose the priority carefully when registering a handler for this event:
 * TODO: Less fragile way to organize this
 *
 *      LOWEST      Things that cancel the login
 *      LOW         Silent initialization of player state
 *      NORMAL      Welcome message
 *      HIGH        Alerts
 *      HIGHEST     Private messages
 *      MONITOR     Nickname reminder
 *
 * NOTE: No handler should un-cancel the login once it has been cancelled,
 * because some handlers may have already ignored the event.
 *
 */
public class UserLoginEvent extends Event implements UserEvent {

    private final Player player;
    private final LoginResponse response;

    private PlayerLoginEvent.Result result;
    private @Nullable BaseComponent kickMessage;

    public UserLoginEvent(Player player, LoginResponse response, PlayerLoginEvent.Result result, @Nullable BaseComponent kickMessage) {
        this.response = checkNotNull(response);
        this.player = checkNotNull(player);
        this.result = checkNotNull(result);
        this.kickMessage = kickMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public LoginResponse response() {
        return response;
    }

    @Override
    public User getUser() {
        return response.user();
    }

    public @Nullable Session getSession() {
        return response.session();
    }

    public PlayerLoginEvent.Result getResult() {
        return result;
    }

    public @Nullable BaseComponent getKickMessage() {
        return kickMessage;
    }

    public void setKickMessage(@Nullable BaseComponent kickMessage) {
        this.kickMessage = kickMessage;
    }

    public void allow() {
        this.result = PlayerLoginEvent.Result.ALLOWED;
        this.kickMessage = null;
    }

    public void disallow(PlayerLoginEvent.Result result, BaseComponent message) {
        this.result = result;
        this.kickMessage = message;
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
