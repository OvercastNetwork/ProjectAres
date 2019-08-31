package tc.oc.commons.bukkit.chat;

import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.chat.Components;
import tc.oc.commons.core.chat.ImmutableComponent;
import tc.oc.commons.core.util.Utils;

public class HeaderComponent extends ImmutableComponent implements RenderableComponent {

    public static final ChatColor DEFAULT_LINE_COLOR = ChatColor.WHITE;
    public static final int DEFAULT_WIDTH = ChatUtils.MAX_CHAT_WIDTH;

    private final @Nullable BaseComponent content;
    private final ChatColor lineColor;
    private final int width;

    public HeaderComponent() {
        this(DEFAULT_LINE_COLOR);
    }

    public HeaderComponent(@Nullable BaseComponent content) {
        this(DEFAULT_LINE_COLOR, content);
    }

    public HeaderComponent(ChatColor lineColor) {
        this(lineColor, DEFAULT_WIDTH);
    }

    public HeaderComponent(ChatColor lineColor, @Nullable BaseComponent content) {
        this(lineColor, DEFAULT_WIDTH, content);
    }

    public HeaderComponent(ChatColor lineColor, int width) {
        this(lineColor, width, null);
    }

    public HeaderComponent(ChatColor lineColor, int width, @Nullable BaseComponent content) {
        this.content = content;
        this.lineColor = lineColor;
        this.width = width;
    }

    @Override
    public BaseComponent duplicate() {
        return new HeaderComponent(lineColor, width, content == null ? null : content.duplicate());
    }

    @Override
    public BaseComponent duplicateWithoutFormatting() {
        return duplicate();
    }

    public ChatColor getLineColor() {
        return lineColor;
    }

    public int getWidth() {
        return width;
    }

    public @Nullable BaseComponent getContent() {
        return content;
    }

    @Override public BaseComponent render(ComponentRenderContext context, CommandSender viewer) {
        if(content != null) {
            Component content = new Component(Components.space(), context.render(this.content, viewer), Components.space());
            int contentWidth = Components.pixelWidth(content);
            int lineChars = Math.max(0, ((width - contentWidth) / 2 + 1) / (ChatUtils.SPACE_PIXEL_WIDTH + 1));
            Component line = new Component(Strings.repeat(" ", lineChars), lineColor, ChatColor.STRIKETHROUGH);
            return new Component(line, content, line);
        } else {
            int lineChars = Math.max(0, (width + 1) / (ChatUtils.SPACE_PIXEL_WIDTH + 1));
            return new Component(Strings.repeat(" ", lineChars), lineColor, ChatColor.STRIKETHROUGH);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lineColor, width, content);
    }

    @Override
    protected boolean equals(BaseComponent obj) {
        return Utils.equals(HeaderComponent.class, this, obj, that ->
            lineColor.equals(that.getLineColor()) &&
            width == that.getWidth() &&
            super.equals(that) &&
            Objects.equals(content, that.getContent())
        );
    }
}
