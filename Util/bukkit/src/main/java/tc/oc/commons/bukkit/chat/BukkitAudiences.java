package tc.oc.commons.bukkit.chat;

import javax.inject.Singleton;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.MinecraftAudiences;
import tc.oc.commons.core.chat.NullAudience;

import static tc.oc.minecraft.protocol.MinecraftVersion.lessThan;
import static tc.oc.minecraft.protocol.MinecraftVersion.MINECRAFT_1_8;

@Singleton
public class BukkitAudiences extends MinecraftAudiences<CommandSender> implements Audiences {

    @Deprecated
    public static Audience getAudience(CommandSender sender) {
        if(sender == null) {
            return NullAudience.INSTANCE;
        } if(sender instanceof Player) {
            Player player = (Player) sender;
            if(lessThan(MINECRAFT_1_8, player.getProtocolVersion())) {
                return new LegacyPlayerAudience(player);
            } else {
                return new PlayerAudience(player);
            }
        } else if(sender instanceof ConsoleCommandSender) {
            return new ConsoleAudience(sender.getServer());
        } else {
            return new CommandSenderAudience(sender);
        }
    }

    @Override
    public Audience get(CommandSender viewer) {
        return getAudience(viewer);
    }
}
