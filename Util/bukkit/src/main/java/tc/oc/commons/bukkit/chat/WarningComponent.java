package tc.oc.commons.bukkit.chat;

import java.util.Objects;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.chat.ImmutableComponent;
import tc.oc.commons.core.util.Utils;

import static com.google.common.base.Preconditions.checkNotNull;

public class WarningComponent extends ImmutableComponent implements RenderableComponent {

    private final BaseComponent content;

    public WarningComponent(BaseComponent content) {
        this.content = checkNotNull(content);
    }

    public WarningComponent(String translate, Object... with) {
        this(new TranslatableComponent(translate, with));
    }

    public BaseComponent content() {
        return content;
    }

    @Override
    public BaseComponent duplicate() {
        return new WarningComponent(content.duplicate());
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }

    @Override
    public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
        return Components.warning(context.render(content, viewer));
    }

    @Override
    protected boolean equals(BaseComponent obj) {
        return Utils.equals(WarningComponent.class, this, obj, that ->
            super.equals(that) &&
            content.equals(that.content())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), content);
    }
}
