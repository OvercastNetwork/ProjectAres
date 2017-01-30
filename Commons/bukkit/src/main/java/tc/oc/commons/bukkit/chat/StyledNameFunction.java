package tc.oc.commons.bukkit.chat;

import com.google.common.base.Function;
import net.md_5.bungee.api.chat.BaseComponent;

public class StyledNameFunction implements Function<Named, BaseComponent> {
    private final NameStyle style;

    public StyledNameFunction(NameStyle style) {
        this.style = style;
    }

    @Override
    public BaseComponent apply(Named named) {
        return named.getStyledName(style);
    }
}
