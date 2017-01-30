package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

public class UserTextComponentRenderer implements ComponentRenderer<UserTextComponent> {
    @Override
    public BaseComponent render(ComponentRenderContext context, UserTextComponent original, CommandSender viewer) {
        return original.rendered;
    }
}
