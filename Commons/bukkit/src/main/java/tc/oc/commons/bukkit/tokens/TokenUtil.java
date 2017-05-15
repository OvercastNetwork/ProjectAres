package tc.oc.commons.bukkit.tokens;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.users.UserService;
import tc.oc.commons.bukkit.util.SyncPlayerExecutorFactory;
import tc.oc.api.docs.User;

import javax.inject.Inject;

public class TokenUtil {

    @Inject
    private static BukkitUserStore userStore;
    @Inject
    private static UserService userService;
    @Inject
    private static SyncPlayerExecutorFactory playerExecutorFactory;

    public static User getUser(Player player) {
        return userStore.getUser(player);
    }

    public static void giveMapTokens(PlayerId playerId, int count) {
        playerExecutorFactory.queued(playerId).callback(
                userService.creditMaptokens(playerId, count),
                (player, update) -> {}
        );

    }

    public static void giveMutationTokens(PlayerId playerId, int count) {
        playerExecutorFactory.queued(playerId).callback(
                userService.creditMutationtokens(playerId, count),
                (player, update) -> {}
        );
    }
}
