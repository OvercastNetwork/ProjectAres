package tc.oc.commons.bukkit.nick;

import javax.annotation.Nullable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.api.users.UserSearchResponse;

public interface IdentityProvider {

    Identity createIdentity(PlayerId playerId, @Nullable String nickname);

    default Identity createIdentity(PlayerId playerId) {
        return createIdentity(playerId, null);
    }

    Identity createIdentity(Player player, @Nullable String nickname);

    Identity createIdentity(CommandSender player);

    Identity createIdentity(Session session);

    Identity createIdentity(UserSearchResponse response);

    Identity currentIdentity(CommandSender player);

    Identity currentIdentity(PlayerId playerId);

    default Identity currentOrConsoleIdentity(@Nullable PlayerId playerId) {
        return playerId == null ? consoleIdentity() : currentIdentity(playerId);
    }

    Identity consoleIdentity();

    @Nullable Identity onlineIdentity(String name);

    void changeIdentity(Player player, @Nullable String nickname);

    boolean revealAll(CommandSender viewer);

    boolean reveal(CommandSender viewer, UserId userId);
}
