package tc.oc.commons.bukkit.chat;

import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;

/**
 * Recursively renders any child components that are common to all {@link BaseComponent}
 * subtypes, which includes the "extra" array, and "hoverEvent.value". Also copies formatting
 * fields to the rendered component, if it is different from the original.
 *
 * Terminal component types should subclass this and implement {@link #renderContent} to render
 * anything besides these common fields.
 */
public abstract class BaseComponentRenderer<T extends BaseComponent> implements ComponentRenderer<T> {
    @Override public BaseComponent render(ComponentRenderContext context, T original, CommandSender viewer) {
        BaseComponent content = renderContent(context, original, viewer);
        if(content != original) {
            // Pass rendered content through the rendering pipeline again,
            // in case it is composed of more renderable components.
            content = context.render(content, viewer);
        }

        // Render extras
        List<BaseComponent> extra = original.getExtra() == null ? null : context.render(original.getExtra(), viewer);

        // If there is a hover event, render its value (which is an array of child components)
        HoverEvent hoverEvent = original.getHoverEvent();
        if(hoverEvent != null && hoverEvent.getValue() != null) {
            BaseComponent[] value = context.render(hoverEvent.getValue(), viewer);
            if(value != hoverEvent.getValue()) {
                hoverEvent = new HoverEvent(hoverEvent.getAction(), value);
            }
        }

        if(content == original && extra == original.getExtra() && hoverEvent == original.getHoverEvent()) {
            // If content, extras, and hoverEvent were not changed by rendering,
            // the original component does not need to change.
            return original;
        } else if(!original.hasFormatting() && extra == content.getExtra()) {
            // If the original has no formatting, and rendered content has the same extra as the original,
            // and extra has no changed, rendered content can be returned on its own
            return content;
        } else if(content == original) {
            // If only child components changed, dupe the original and replace the children
            original = (T) original.duplicate();
            if(extra != null) original.setExtra(extra);
            original.setHoverEvent(hoverEvent);
            return original;
        } else {
            // Combine the rendered content with the original formatting by wrapping it in a new component.
            BaseComponent wrapper = new Component().extra(content);
            Components.copyFormat(original, wrapper);
            wrapper.setClickEvent(original.getClickEvent());
            if(extra != null) {
                for(BaseComponent c : extra) {
                    wrapper.addExtra(c);
                }
            }
            return wrapper;
        }
    }

    protected abstract BaseComponent renderContent(ComponentRenderContext context, T original, CommandSender viewer);
}
