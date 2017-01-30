package tc.oc.commons.bungee.chat;

import javax.inject.Singleton;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tc.oc.commons.core.chat.Audience;
import tc.oc.commons.core.chat.MinecraftAudiences;

@Singleton
public class BungeeAudiences extends MinecraftAudiences<CommandSender> implements Audiences {
    @Override
    public Audience get(CommandSender sender) {
        if(sender instanceof ProxiedPlayer) {
            return new PlayerAudience((ProxiedPlayer) sender);
        } else {
            return new ConsoleAudience();
        }
    }
}
