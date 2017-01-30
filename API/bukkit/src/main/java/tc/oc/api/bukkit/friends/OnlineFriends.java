package tc.oc.api.bukkit.friends;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.UserId;
import tc.oc.commons.core.util.Predicates;

/**
 * Map of friend relationships for currently online players.
 * At least one of the two given players must be online to guarantee no false negatives.
 */
public interface OnlineFriends {

    boolean areFriends(Player a, UserId b);
    boolean areFriends(Player a, Player b);

    Stream<Player> onlineFriends(UserId userId);
    Stream<Player> onlineFriends(Player player);

    default boolean areFriends(CommandSender a, UserId b) {
        return a instanceof Player &&
               areFriends((Player) a, b);
    }

    default boolean areFriends(CommandSender a, CommandSender b) {
        return a instanceof Player &&
               b instanceof Player &&
               areFriends((Player) a, (Player) b);
    }

    default Predicate<CommandSender> areFriends(UserId userId) {
        return friend -> areFriends(friend, userId);
    }

    default Predicate<CommandSender> areFriends(Player player) {
        return friend -> areFriends(friend, player);
    }

    default Predicate<CommandSender> areFriends(CommandSender player) {
        return player instanceof Player ? areFriends((Player) player)
                                        : Predicates.alwaysFalse();
    }

    default Stream<Player> onlineFriends(CommandSender player) {
        return player instanceof Player ? onlineFriends((Player) player)
                                        : Stream.empty();
    }
}
