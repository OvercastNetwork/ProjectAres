package tc.oc.api.bukkit.users;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.UserId;

public class Users {

    @Inject private static BukkitUserStore userStore;

    /**
     * This method is for legacy support ONLY.
     *
     * Use {@link BukkitUserStore#getUser(UserId)} instead.
     */
    @Deprecated
    public static PlayerId playerId(Player player) {
        return userStore.getUser(player);
    }

    /**
     * This method is for legacy support ONLY.
     *
     * Use {@link OnlinePlayers#find(UserId)} instead.
     */
    @Deprecated
    public static @Nullable Player player(PlayerId playerId) {
        return Bukkit.getPlayerExact(playerId.username());
    }

    public static boolean equals(PlayerId playerId, Player player) {
        return playerId.username().equals(player.getName());
    }

    public static boolean equals(PlayerId playerId, CommandSender sender) {
        return sender instanceof Player && equals(playerId, (Player) sender);
    }

    public static boolean isOnline(PlayerId playerId) {
        return player(playerId) != null;
    }
}
