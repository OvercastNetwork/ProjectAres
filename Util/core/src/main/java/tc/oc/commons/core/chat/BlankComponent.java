package tc.oc.commons.core.chat;

import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class BlankComponent extends TextComponent {

    public static final BlankComponent INSTANCE = new BlankComponent();

    private BlankComponent() {
        super("");
    }

    @Override
    public BaseComponent duplicate() {
        return this;
    }

    @Override
    public String toPlainText() {
        return "";
    }

    @Override
    public String toLegacyText() {
        return "";
    }

    @Override
    public List<BaseComponent> getExtra() {
        return Collections.emptyList();
    }

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
