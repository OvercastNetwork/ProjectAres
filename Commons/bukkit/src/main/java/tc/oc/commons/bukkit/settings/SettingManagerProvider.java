package tc.oc.commons.bukkit.settings;

import java.util.Optional;
import me.anxuiz.settings.SettingManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.api.docs.User;

/**
 * Provides access to player settings for both online and offline players
 */
public interface SettingManagerProvider {
    /**
     * Return the {@link SettingManager} for an online player,
     * which can be used to read and write settings.
     */
    SettingManager getManager(Player player);

    default Optional<SettingManager> tryManager(CommandSender sender) {
        return Optional.ofNullable(sender instanceof Player ? getManager((Player) sender) : null);
    }

    /**
     * Return a {@link SettingManager} for the given {@link User}.
     *
     * If the user is currently online, their in-memory manager is returned.
     * Otherwise, the a read-only manager is returned that extracts settings
     * from the given {@link User} document.
     */
    SettingManager getManager(User user);
}
