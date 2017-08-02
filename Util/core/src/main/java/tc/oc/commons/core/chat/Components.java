package tc.oc.commons.core.chat;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utils for working with {@link BaseComponent}s
 */
public abstract class Components {

    public static BaseComponent[] fromLegacyTextMulti(String legacyText) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('`', legacyText));
    }

    /**
     * Convert text with legacy formatting codes to a {@link Component}
     */
    public static Component fromLegacyText(String legacyText) {
        return new Component(fromLegacyTextMulti(legacyText));
    }

    private static final Component SPACE = new Component(" ");
    private static final Component NEWLINE = new Component("\n");

    public static BaseComponent blank() { return BlankComponent.INSTANCE; }
    public static BaseComponent space() { return SPACE; }
    public static BaseComponent newline() { return NEWLINE; }

    public static List<BaseComponent> repeat(Supplier<BaseComponent> component, int amount) {
        return IntStream.rangeClosed(1, amount).mapToObj(i -> component.get()).collect(Collectors.toList());
    }

    public static boolean isBlank(@Nullable BaseComponent c) {
        return c == null || c instanceof BlankComponent;
    }

    /**
     * See {@link #format(MessageFormat, List)}
     */
    public static BaseComponent[] format(String format, BaseComponent... arguments) {
        return format(new MessageFormat(format), arguments);
    }

    /**
     * See {@link #format(MessageFormat, List)}
     */
    public static BaseComponent[] format(String format, List<BaseComponent> arguments) {
        return format(new MessageFormat(format), arguments);
    }

    /**
     * See {@link #format(MessageFormat, List)}
     */
    public static BaseComponent[] format(MessageFormat format, BaseComponent... arguments) {
        return format(format, Arrays.asList(arguments));
    }

    /**
     * Render the given {@link MessageFormat} to component form, using the given arguments.
     * This is equivalent to {@link MessageFormat#format} but a component tree is generated,
     * instead of a String, and the arguments are included directly in the tree.
     *
     * To accomplish this, the message is first rendered to a String with placeholder arguments.
     * Then {@link MessageFormat#formatToCharacterIterator} is used to figure out where the
     * arguments appear in the result, and the text between them is spliced out and used to
     * build the component tree along with the actual arguments.
     */
    public static BaseComponent[] format(MessageFormat format, List<BaseComponent> arguments) {
        if(arguments == null || arguments.isEmpty()) {
            return new BaseComponent[] { new TextComponent(format.format(null, new StringBuffer(), null).toString()) };
        }

        List<BaseComponent> parts = new ArrayList<>(arguments.size() * 2 + 1);

        Object[] dummies = new Object[arguments.size()];
        StringBuffer sb = format.format(dummies, new StringBuffer(), null);
        AttributedCharacterIterator iter = format.formatToCharacterIterator(dummies);

        while(iter.getIndex() < iter.getEndIndex()) {
            int end = iter.getRunLimit();
            Integer index = (Integer) iter.getAttribute(MessageFormat.Field.ARGUMENT);
            if(index == null) {
                parts.add(new TextComponent(sb.substring(iter.getIndex(), end)));
            } else {
                parts.add(arguments.get(index));
            }
            iter.setIndex(end);
        }

        return parts.toArray(new BaseComponent[parts.size()]);
    }

    /**
     * Recursively compare the given components for equality
     */
    public static boolean equals(HoverEvent a, HoverEvent b) {
        return (a == b) ||
               (a != null && b != null &&
                a.getAction() == b.getAction() &&
                equals(a.getValue(), b.getValue()));
    }

    /**
     * Recursively compare the given components for equality
     */
    public static boolean equals(ClickEvent a, ClickEvent b) {
        return (a == b) ||
               (a != null && b != null &&
                a.getAction() == b.getAction() &&
                Objects.equals(a.getValue(), b.getValue()));
    }

    /**
     * Recursively compare the given components for equality
     */
    public static boolean equals(BaseComponent a, BaseComponent b) {
        return (a == b) ||
               (a != null && b != null &&
                Objects.equals(a.isBoldRaw(), b.isBoldRaw()) &&
                Objects.equals(a.isItalicRaw(), b.isItalicRaw()) &&
                Objects.equals(a.isObfuscatedRaw(), b.isItalicRaw()) &&
                Objects.equals(a.isStrikethroughRaw(), b.isItalicRaw()) &&
                Objects.equals(a.isUnderlinedRaw(), b.isItalicRaw()) &&
                equals(a.getClickEvent(), b.getClickEvent()) &&
                equals(a.getHoverEvent(), b.getHoverEvent()));
    }

    /**
     * Recursively compare the given components for equality
     */
    public static boolean equals(BaseComponent[] a, BaseComponent[] b) {
        if(a == b) return true;
        if(a == null || b == null) return false;
        if(a.length != b.length) return false;

        for(int i = 0; i < a.length; i++) {
            if(!equals(a[i], b[i])) return false;
        }

        return true;
    }

    /**
     * Recursively compare the given components for equality
     */
    public static boolean equals(Collection<BaseComponent> a, Collection<BaseComponent> b) {
        if(a == b) return true;
        if(a == null || b == null) return false;
        if(a.size() != b.size()) return false;

        for(Iterator<BaseComponent> ia = a.iterator(), ib = b.iterator(); ia.hasNext() && ib.hasNext(); ) {
            if(!equals(ia.next(), ib.next())) return false;
        }

        return true;
    }

    public static BaseComponent color(BaseComponent c, @Nullable ChatColor color) {
        c.setColor(color);
        return c;
    }

    public static BaseComponent bold(BaseComponent c, @Nullable Boolean yes) {
        c.setBold(yes);
        return c;
    }

    public static BaseComponent italic(BaseComponent c, @Nullable Boolean yes) {
        c.setItalic(yes);
        return c;
    }

    public static BaseComponent underlined(BaseComponent c, @Nullable Boolean yes) {
        c.setUnderlined(yes);
        return c;
    }

    public static BaseComponent strikethrough(BaseComponent c, @Nullable Boolean yes) {
        c.setStrikethrough(yes);
        return c;
    }

    public static BaseComponent obfuscated(BaseComponent c, @Nullable Boolean yes) {
        c.setObfuscated(yes);
        return c;
    }

    public static BaseComponent clickEvent(BaseComponent c, @Nullable ClickEvent event) {
        c.setClickEvent(event);
        return c;
    }

    public static BaseComponent clickEvent(BaseComponent c, ClickEvent.Action action, String value) {
        c.setClickEvent(new ClickEvent(action, value));
        return c;
    }

    public static BaseComponent hoverEvent(BaseComponent c, @Nullable HoverEvent event) {
        c.setHoverEvent(event);
        return c;
    }

    public static BaseComponent hoverEvent(BaseComponent c, HoverEvent.Action action, BaseComponent...values) {
        c.setHoverEvent(new HoverEvent(action, values));
        return c;
    }

    public static BaseComponent extra(BaseComponent c, BaseComponent...extras) {
        for(BaseComponent extra : extras) {
            checkNotNull(extra);
            c.addExtra(extra);
        }
        return c;
    }

    public static BaseComponent addFormats(BaseComponent component, ChatColor... formats) {
        return addFormats(component, Arrays.asList(formats));
    }

    public static BaseComponent addFormats(BaseComponent component, Iterable<ChatColor> formats) {
        for(ChatColor format : formats) {
            checkNotNull(format);
            switch(format) {
                case BOLD:          component.setBold(true); break;
                case ITALIC:        component.setItalic(true); break;
                case UNDERLINE:     component.setUnderlined(true); break;
                case STRIKETHROUGH: component.setStrikethrough(true); break;
                case MAGIC:         component.setObfuscated(true); break;
                case RESET:         throw new IllegalArgumentException("Cannot add format " + format);
                default:            component.setColor(format); break;
            }
        }
        return component;
    }

    public static BaseComponent removeFormats(BaseComponent component, ChatColor... formats) {
        for(ChatColor format : formats) {
            checkNotNull(format);
            switch(format) {
                case BOLD:          component.setBold(false); break;
                case ITALIC:        component.setItalic(false); break;
                case UNDERLINE:     component.setUnderlined(false); break;
                case STRIKETHROUGH: component.setStrikethrough(false); break;
                case MAGIC:         component.setObfuscated(false); break;
                default:            throw new IllegalArgumentException("Cannot remove format " + format);
            }
        }
        return component;
    }

    public static boolean hasFormat(BaseComponent c) {
        return c.getColorRaw() != null ||
               c.isBoldRaw() != null ||
               c.isItalicRaw() != null ||
               c.isUnderlinedRaw() != null ||
               c.isStrikethroughRaw() != null ||
               c.isObfuscatedRaw() != null ||
               c.getClickEvent() != null ||
               c.getHoverEvent() != null;
    }

    public static void copyFormat(BaseComponent from, BaseComponent to) {
        to.setColor(from.getColorRaw());
        to.setBold(from.isBoldRaw());
        to.setItalic(from.isItalicRaw());
        to.setUnderlined(from.isUnderlinedRaw());
        to.setStrikethrough(from.isStrikethroughRaw());
        to.setObfuscated(from.isObfuscatedRaw());
    }

    public static void copyEvents(BaseComponent from, BaseComponent to) {
        to.setClickEvent(from.getClickEvent());
        to.setHoverEvent(from.getHoverEvent());
    }

    public static void copyFormatAndEvents(BaseComponent from, BaseComponent to) {
        copyFormat(from, to);
        copyEvents(from, to);
    }

    public static void softMergeFormat(BaseComponent from, BaseComponent to) {
        if(to.getColorRaw() == null) to.setColor(from.getColorRaw());
        if(to.isBoldRaw() == null) to.setBold(from.isBoldRaw());
        if(to.isItalicRaw() == null) to.setItalic(from.isItalicRaw());
        if(to.isUnderlinedRaw() == null) to.setUnderlined(from.isUnderlinedRaw());
        if(to.isStrikethroughRaw() == null) to.setStrikethrough(from.isStrikethroughRaw());
        if(to.isObfuscatedRaw() == null) to.setObfuscated(from.isObfuscatedRaw());
        if(to.getClickEvent() == null) to.setClickEvent(from.getClickEvent());
        if(to.getHoverEvent() == null) to.setHoverEvent(from.getHoverEvent());
    }

    public static void hardMergeFormat(BaseComponent from, BaseComponent to) {
        if(from.getColorRaw() != null) to.setColor(from.getColorRaw());
        if(from.isBoldRaw() != null) to.setBold(from.isBoldRaw());
        if(from.isItalicRaw() != null) to.setItalic(from.isItalicRaw());
        if(from.isUnderlinedRaw() != null) to.setUnderlined(from.isUnderlinedRaw());
        if(from.isStrikethroughRaw() != null) to.setStrikethrough(from.isStrikethroughRaw());
        if(from.isObfuscatedRaw() != null) to.setObfuscated(from.isObfuscatedRaw());
        if(from.getClickEvent() != null) to.setClickEvent(from.getClickEvent());
        if(from.getHoverEvent() != null) to.setHoverEvent(from.getHoverEvent());
    }

    public static void copyLastFormat(String legacy, BaseComponent to) {
        int length = legacy.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = legacy.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < length - 1) {
                char c = legacy.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    addFormats(to, color);

                    // Once we find a color or reset we can stop searching
                    if(color.equals(ChatColor.RESET)) break;
                    if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) break;
                }
            }
        }
    }

    public static void addExtra(BaseComponent c, List<BaseComponent> extra) {
        if(extra != null) {
            if(c.getExtra() == null) {
                c.setExtra(extra);
            } else {
                c.getExtra().addAll(extra);
            }
        }
    }

    public static BaseComponent shallowCopy(BaseComponent original) {
        if(original instanceof TextComponent) {
            return shallowCopy((TextComponent) original);
        } else if(original instanceof TranslatableComponent) {
            return shallowCopy((TranslatableComponent) original);
        } else {
            throw new IllegalArgumentException("Don't know how to copy a " + original.getClass().getName());
        }
    }

    public static TextComponent shallowCopy(TextComponent original) {
        TextComponent copy = new TextComponent(original.getText());
        copyFormatAndEvents(original, copy);
        copy.setExtra(original.getExtra());
        return copy;
    }

    public static TranslatableComponent shallowCopy(TranslatableComponent original) {
        TranslatableComponent copy = new TranslatableComponent(original.getTranslate());
        copy.setWith(original.getWith());
        copyFormatAndEvents(original, copy);
        copy.setExtra(original.getExtra());
        return copy;
    }

    public static BaseComponent concat(BaseComponent... components) {
        switch(components.length) {
            case 0: return blank();
            case 1: return components[0];
            default: return new Component(components);
        }
    }

    public static BaseComponent link(String protocol, String host, String path, ChatColor...formats) {
        try {
            return link(new URL(protocol, host, path), formats);
        } catch(MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static BaseComponent link(URL url, ChatColor...formats) {
        try {
            final URI uri = url.toURI();

            // The encoded form escapes all illegal characters e.g. " " becomes "%20",
            // which is required by the client.
            final String encoded = url.toExternalForm();

            // The display form leaves the illegal chars in the path, which tends to look nicer.
            // It also removes any trailing slash.
            final String display = encoded
                .replace(uri.getRawPath(), uri.getPath())
                .replaceAll("/$", "");

            final Component c = new Component(display).clickEvent(ClickEvent.Action.OPEN_URL, encoded);
            if(formats.length == 0) {
                c.color(ChatColor.BLUE).underlined(true);
            } else {
                c.add(formats);
            }
            return c;
        } catch(URISyntaxException e) {
            return blank();
        }
    }

    public static int pixelWidth(Collection<BaseComponent> components, boolean bold) {
        int width = 0;
        for(BaseComponent component : components) {
            width += pixelWidth(component);
        }
        return width;
    }

    public static int pixelWidth(BaseComponent component, boolean bold) {
        if(component.isBoldRaw() != null) {
            bold = component.isBold();
        }
        int width = 0;
        if(component instanceof TextComponent) {
            String text = ((TextComponent) component).getText();
            width += ChatUtils.pixelWidth(text, bold);
        }
        if(component.getExtra() != null) {
            width += pixelWidth(component.getExtra(), bold);
        }
        return width;
    }

    public static int pixelWidth(BaseComponent component) {
        return pixelWidth(component, false);
    }

    public static List<BaseComponent> transform(List<String> strings) {
        return Lists.transform(strings, new Function<String, BaseComponent>() {
            @Override public BaseComponent apply(String s) {
                return new Component(s);
            }
        });
    }

    public static BaseComponent join(BaseComponent delimiter, Collection<? extends BaseComponent> elements) {
        Component c = new Component();
        boolean first = true;
        for(BaseComponent el : elements) {
            if(!first) {
                c.extra(delimiter);
            }
            c.extra(el);
            first = false;
        }
        return c;
    }

    public static List<BaseComponent> wordWrap(BaseComponent text, int width) {
        return wordWrap(new ArrayList<>(), text, width);
    }

    public static List<BaseComponent> wordWrap(List<BaseComponent> lines, BaseComponent text, int width) {
        for(String line : ChatUtils.wordWrap(text.toLegacyText(), width)) {
            lines.add(concat(TextComponent.fromLegacyText(line)));
        }
        return lines;
    }

    public static BaseComponent naturalList(Stream<? extends BaseComponent> elements) {
        return naturalList(elements.collect(Collectors.toList()));
    }

    public static BaseComponent naturalList(Collection<? extends BaseComponent> elements) {
        switch(elements.size()) {
            case 0: return Components.blank();
            case 1: return elements.iterator().next();
            case 2:
                return new TranslatableComponent("misc.list.pair", elements.toArray());
            default:
                Iterator<? extends BaseComponent> iter = elements.iterator();
                BaseComponent a = new TranslatableComponent("misc.list.start", iter.next(), iter.next());
                BaseComponent b = iter.next();
                while(iter.hasNext()) {
                    a = new TranslatableComponent("misc.list.middle", a, b);
                    b = iter.next();
                }
                return new TranslatableComponent("misc.list.end", a, b);
        }
    }

    public static BaseComponent warning(BaseComponent content) {
        return new Component(ChatColor.RED).extra(new Component(" \u26a0 ", ChatColor.YELLOW), content);
    }
}
