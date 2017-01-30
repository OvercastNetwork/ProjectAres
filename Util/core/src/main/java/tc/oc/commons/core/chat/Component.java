package tc.oc.commons.core.chat;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Souped up {@link TextComponent}
 */
public class Component extends TextComponent {

    public Component() {
        super("");
    }

    public Component(String text, ChatColor... formats) {
        super(checkNotNull(text));
        this.add(formats);
    }

    public Component(Number number, ChatColor... formats) {
        super(checkNotNull(number).toString());
        this.add(formats);
    }

    public Component(BaseComponent extra, ChatColor format, ChatColor... formats) {
        super("");
        addExtra(extra);
        add(format);
        add(formats);
    }

    public Component(Iterable<BaseComponent> extras, ChatColor... formats) {
        super("");
        setExtra(Lists.newArrayList(extras));
        this.add(formats);
    }

    public Component(Stream<BaseComponent> extras, ChatColor... formats) {
        super("");
        setExtra(extras.collect(Collectors.toList()));
        this.add(formats);
    }

    public Component(ChatColor... formats) {
        this();
        this.add(formats);
    }

    public Component(BaseComponent... extras) {
        super(extras);
    }

    public Component(int extraCapacity) {
        this();
        setExtra(new ArrayList<>(extraCapacity));
    }

    /**
     * This is private so it doesn't accidentally get called instead of {@link #Component(BaseComponent...)}
     */
    private Component(Component original) {
        super(original);
    }

    @Override
    public Component duplicate() {
        return new Component(this);
    }

    public static Component inheritFormatFrom(BaseComponent component) {
        Component format = new Component();
        format.setColor(component.getColor());
        format.setBold(component.isBold());
        format.setItalic(component.isItalic());
        format.setUnderlined(component.isUnderlined());
        format.setStrikethrough(component.isStrikethrough());
        format.setObfuscated(component.isObfuscated());
        format.setClickEvent(component.getClickEvent());
        format.setHoverEvent(component.getHoverEvent());
        return format;
    }

    public static Component copyFormatFrom(BaseComponent component) {
        Component format = new Component();
        format.setColor(component.getColorRaw());
        format.setBold(component.isBoldRaw());
        format.setItalic(component.isItalicRaw());
        format.setUnderlined(component.isUnderlinedRaw());
        format.setStrikethrough(component.isStrikethroughRaw());
        format.setObfuscated(component.isObfuscatedRaw());
        format.setClickEvent(component.getClickEvent());
        format.setHoverEvent(component.getHoverEvent());
        return format;
    }

    public void applyFormatTo(BaseComponent component) {
        component.setColor(this.getColorRaw());
        component.setBold(this.isBoldRaw());
        component.setItalic(this.isItalicRaw());
        component.setUnderlined(this.isUnderlinedRaw());
        component.setStrikethrough(this.isStrikethroughRaw());
        component.setObfuscated(this.isObfuscatedRaw());
        component.setClickEvent(this.getClickEvent());
        component.setHoverEvent(this.getHoverEvent());
    }

    public void mergeFormatTo(BaseComponent component) {
        if(this.getColorRaw() != null) component.setColor(this.getColor());
        if(this.isBoldRaw() != null) component.setBold(this.isBoldRaw());
        if(this.isItalicRaw() != null) component.setItalic(this.isItalicRaw());
        if(this.isUnderlinedRaw() != null) component.setUnderlined(this.isUnderlinedRaw());
        if(this.isStrikethroughRaw() != null) component.setStrikethrough(this.isStrikethroughRaw());
        if(this.isObfuscatedRaw() != null) component.setObfuscated(this.isObfuscatedRaw());
        if(this.getClickEvent() != null) component.setClickEvent(this.getClickEvent());
        if(this.getHoverEvent() != null) component.setHoverEvent(this.getHoverEvent());
    }

    public Component clearFormat() {
        this.setColor(null);
        this.setBold(null);
        this.setItalic(null);
        this.setUnderlined(null);
        this.setStrikethrough(null);
        this.setObfuscated(null);
        this.setClickEvent(null);
        this.setHoverEvent(null);
        return this;
    }

    public Component add(ChatColor...formats) {
        Components.addFormats(this, formats);
        return this;
    }

    public Component add(Iterable<ChatColor> formats) {
        Components.addFormats(this, formats);
        return this;
    }

    public Component remove(ChatColor...formats) {
        Components.removeFormats(this, formats);
        return this;
    }

    public Component color(@Nullable ChatColor color) {
        this.setColor(color);
        return this;
    }

    public Component bold(@Nullable Boolean yes) {
        this.setBold(yes);
        return this;
    }

    public Component italic(@Nullable Boolean yes) {
        this.setItalic(yes);
        return this;
    }

    public Component underlined(@Nullable Boolean yes) {
        this.setUnderlined(yes);
        return this;
    }

    public Component strikethrough(@Nullable Boolean yes) {
        this.setStrikethrough(yes);
        return this;
    }

    public Component obfuscated(@Nullable Boolean yes) {
        this.setObfuscated(yes);
        return this;
    }

    public Component clickEvent(@Nullable ClickEvent event) {
        this.setClickEvent(event);
        return this;
    }

    public Component clickEvent(ClickEvent.Action action, String value) {
        this.setClickEvent(new ClickEvent(action, value));
        return this;
    }

    public Component clickEvent(URL url) {
        return clickEvent(ClickEvent.Action.OPEN_URL, url.toExternalForm());
    }

    public Component hoverEvent(@Nullable HoverEvent event) {
        this.setHoverEvent(event);
        return this;
    }

    public Component hoverEvent(HoverEvent.Action action, BaseComponent...values) {
        this.setHoverEvent(new HoverEvent(action, values));
        return this;
    }

    public Component hoverEvent(BaseComponent text) {
        return hoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }

    public Component link(URL url, ChatColor... formats) {
        return extra(Components.link(url, formats));
    }

    public Component text(String text) {
        this.setText(text);
        return this;
    }

    public Component extra(BaseComponent...extras) {
        return this.extra(Arrays.asList(extras));
    }

    public Component extra(String extra, ChatColor... formats) {
        return extra(Arrays.asList(new Component(extra, formats)));
    }

    public Component extra(Number extra, ChatColor... formats) {
        return extra(Arrays.asList(new Component(extra, formats)));
    }

    public Component extra(BaseComponent extra, ChatColor format, ChatColor... formats) {
        return extra(Arrays.asList(new Component(extra, format, formats)));
    }

    public Component extra(Collection<BaseComponent> extras) {
        if(this.getExtra() == null) {
            this.setExtra(new ArrayList<>(extras));
        } else {
            this.setExtra(Lists.newArrayList(Iterables.concat(this.getExtra(), extras)));
        }
        return this;
    }

    public Component translate(String key, Object... with) {
        return extra(new TranslatableComponent(key, with));
    }
}
