package tc.oc.commons.bukkit.chat;

import java.util.Collection;
import javax.inject.Inject;

import com.google.common.reflect.TypeParameter;
import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.commons.core.reflect.Types;

import static com.google.common.base.Preconditions.checkState;

/**
 * @Inject a {@link ComponentRenderContext}
 */
@Deprecated
public class ComponentRenderers {

    @Inject static ComponentRenderContext renderer;

    private ComponentRenderers() {}

    private static ComponentRenderContext renderer() {
        checkState(renderer != null, ComponentRenderContext.class + " not initialized");
        return renderer;
    }

    public static BaseComponent render(BaseComponent component, CommandSender viewer) {
        return renderer().render(component, viewer);
    }

    public static void send(Player viewer, BaseComponent component) {
        viewer.sendMessage(render(component, viewer));
    }

    public static void send(Player viewer, Collection<? extends BaseComponent> components) {
        for(BaseComponent component : components) {
            viewer.sendMessage(render(component, viewer));
        }
    }

    // TODO: when CommandSender has component support...
    public static void send(CommandSender viewer, BaseComponent component) {
        if(viewer instanceof Player) {
            send((Player) viewer, component);
        } else {
            viewer.sendMessage(render(component, viewer));
        }
    }

    public static void send(CommandSender viewer, Collection<? extends BaseComponent> components) {
        if(viewer instanceof Player) {
            send((Player) viewer, components);
        } else {
            for(BaseComponent component : components) {
                viewer.sendMessage(render(component, viewer));
            }
        }
    }

    /**
     * Convert to legacy text. Just calls {@link BaseComponent#toLegacyText} for now,
     * but we may have to replace that at some point due to its dependence on the parent field.
     */
    public static String toLegacyText(BaseComponent component, CommandSender viewer) {
        return render(component, viewer).toLegacyText();
    }

    /**
     * Return a {@link TypeLiteral} describing the required renderer type for the given component type
     */
    public static <T extends BaseComponent> TypeLiteral<ComponentRenderer<? super T>> rendererType(Class<T> componentType) {
        return Types.resolve(new TypeLiteral<ComponentRenderer<? super T>>(){},
                             new TypeParameter<T>(){},
                             componentType);
    }
}
