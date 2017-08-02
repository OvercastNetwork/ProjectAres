package tc.oc.api.bukkit.users;

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
