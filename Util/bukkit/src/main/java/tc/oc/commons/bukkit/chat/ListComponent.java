package tc.oc.commons.bukkit.chat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.chat.ImmutableComponent;
import tc.oc.commons.core.util.Utils;

public class ListComponent extends ImmutableComponent implements RenderableComponent {

    private final Collection<? extends BaseComponent> elements;
    private @Nullable BaseComponent expanded;

    public ListComponent(Collection<? extends BaseComponent> elements) {
        this.elements = elements;
    }

    public <T> ListComponent(Collection<T> elements, Function<? super T, BaseComponent> mapper) {
        this(elements.stream().map(mapper));
    }

    public ListComponent(Stream<? extends BaseComponent> elements) {
        this(elements.collect(Collectors.toList()));
    }

    public ListComponent(BaseComponent... elements) {
        this(Arrays.asList(elements));
    }

    public Collection<? extends BaseComponent> elements() {
        return elements;
    }

    @Override
    public BaseComponent duplicate() {
        return new ListComponent(elements);
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }

    @Override
    public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
        if(expanded == null) {
            expanded = Components.naturalList(elements);
        }
        return context.render(expanded, viewer);
    }

    @Override
    protected boolean equals(BaseComponent obj) {
        return Utils.equals(ListComponent.class, this, obj, that ->
            super.equals(that) &&
            elements.equals(that.elements())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elements);
    }
}
