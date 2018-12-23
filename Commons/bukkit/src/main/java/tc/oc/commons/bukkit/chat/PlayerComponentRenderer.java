package tc.oc.commons.bukkit.chat;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

/**
 * Ties together the {@link ComponentRenderer} and {@link NameRenderer} systems to
 * convert {@link PlayerComponent}s into primitive components.
 */
@Singleton
public class PlayerComponentRenderer extends BaseComponentRenderer<PlayerComponent> {

    private final CachingNameRenderer nameRenderer;

    @Inject PlayerComponentRenderer(CachingNameRenderer nameRenderer) {
        this.nameRenderer = nameRenderer;
    }

    @Override
    public BaseComponent renderContent(ComponentRenderContext context, PlayerComponent original, CommandSender viewer) {
        return nameRenderer.getComponentName(original.getIdentity(),
                                             new NameType(original.getStyle(), original.getIdentity(), viewer));
    }
}
