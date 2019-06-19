package tc.oc.commons.bukkit.chat;

import javax.inject.Singleton;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

@Singleton
public class TextComponentRenderer extends BaseComponentRenderer<TextComponent> {
    @Override protected BaseComponent renderContent(ComponentRenderContext context, TextComponent original, CommandSender viewer) {
        return original;
    }
}
