package tc.oc.commons.core.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.List;

/**
 * An abstract component that disallows any modification, to be used as a base
 * for custom component types that don't want other things messing with their
 * formatting properties.
 *
 * This class overrides all the mutating methods in {@link BaseComponent} and
 * throws {@link UnsupportedOperationException} from them. As such, any code
 * that works with components that it did not create itself should not try to
 * mutate them, because it may get passed one of these. This is probably a good
 * policy in any case.
 *
 * This class also overrides all the format getters and delegates them directly
 * to their "raw" equivalents. This is done because the superclass has a
 * protected field called "parent" that is used to inherit formatting from the
 * component that contains it. Obviously, this will not work with an immutable,
 * shared component. The field is written to directly, and there is no way for
 * a subclass to prevent it, so instead we just override all the methods that
 * depend on the field and ignore it.
 *
 * This might break {@link #toLegacyText}, which expects components to know their
 * own inherited properties, but otherwise shouldn't be a problem.
 */
public abstract class ImmutableComponent extends BaseComponent {

    @Override
    public BaseComponent duplicate() {
        // Assume immutable components are shareable
        return this;
    }

    @Override public ChatColor getColor()       { return super.getColorRaw(); }
    @Override public boolean isBold()           { return super.isBoldRaw(); }
    @Override public boolean isItalic()         { return super.isItalicRaw(); }
    @Override public boolean isUnderlined()     { return super.isUnderlinedRaw(); }
    @Override public boolean isStrikethrough()  { return super.isStrikethroughRaw(); }
    @Override public boolean isObfuscated()     { return super.isObfuscatedRaw(); }

    @Override public void setExtra(List<BaseComponent> components)  { throwImmutable(); }
    @Override public void addExtra(String text)                     { throwImmutable(); }
    @Override public void addExtra(BaseComponent component)         { throwImmutable(); }
    @Override public void setColor(ChatColor color)                 { throwImmutable(); }
    @Override public void setBold(Boolean bold)                     { throwImmutable(); }
    @Override public void setItalic(Boolean italic)                 { throwImmutable(); }
    @Override public void setUnderlined(Boolean underlined)         { throwImmutable(); }
    @Override public void setStrikethrough(Boolean strikethrough)   { throwImmutable(); }
    @Override public void setObfuscated(Boolean obfuscated)         { throwImmutable(); }
    @Override public void setClickEvent(ClickEvent clickEvent)      { throwImmutable(); }
    @Override public void setHoverEvent(HoverEvent hoverEvent)      { throwImmutable(); }

    protected void throwImmutable() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }
}
