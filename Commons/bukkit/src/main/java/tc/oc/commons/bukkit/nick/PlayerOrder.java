package tc.oc.commons.bukkit.nick;

import java.util.Comparator;
import java.util.function.Function;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The order that {@link Player}s appear in the player list, and various other places.
 */
public interface PlayerOrder extends Comparator<Player> {
    interface Factory extends Function<CommandSender, PlayerOrder> {}
}
