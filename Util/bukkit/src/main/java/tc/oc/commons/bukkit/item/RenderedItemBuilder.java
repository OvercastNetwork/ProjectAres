package tc.oc.commons.bukkit.item;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;

/**
 * Extends {@link ItemBuilder} with server-rendered component support.
 *
 * Instances are created for a specific viewer, and all text is rendered for that viewer.
 */
public class RenderedItemBuilder<S extends RenderedItemBuilder<?>> extends ItemBuilder<S> {

    public interface Factory {
        RenderedItemBuilder<?> create(CommandSender viewer);

        RenderedItemBuilder<?> create(CommandSender viewer, ItemStack stack);
    }

    private final CommandSender viewer;
    private final ComponentRenderContext renderer;

    @AssistedInject protected RenderedItemBuilder(@Assisted CommandSender viewer, ComponentRenderContext renderer) {
        this(viewer, new ItemStack(Material.AIR), renderer);
    }

    @AssistedInject protected RenderedItemBuilder(@Assisted CommandSender viewer, @Assisted ItemStack stack, ComponentRenderContext renderer) {
        super(stack);
        this.viewer = viewer;
        this.renderer = renderer;
    }

    public S name(BaseComponent name) {
        return name(renderer.renderLegacy(name, viewer));
    }

    public S lore(BaseComponent lore) {
        return lore(renderer.renderLegacy(lore, viewer));
    }
}
