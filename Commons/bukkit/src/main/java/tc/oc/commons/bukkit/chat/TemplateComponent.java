package tc.oc.commons.bukkit.chat;

import java.util.List;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.chat.ImmutableComponent;

public class TemplateComponent extends ImmutableComponent implements RenderableComponent {

    private final MessageTemplate message;
    private final List<BaseComponent> with;

    public TemplateComponent(MessageTemplate message, BaseComponent... with) {
        this(message, ImmutableList.copyOf(with));
    }

    public TemplateComponent(MessageTemplate message, List<BaseComponent> with) {
        this.message = message;
        this.with = ImmutableList.copyOf(with);
    }

    @Override
    public BaseComponent duplicate() {
        return new TemplateComponent(message, with);
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }

    @Override
    public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
        return new Component(Components.format(message.format(viewer),
                                               context.render(with, viewer)));
    }
}
