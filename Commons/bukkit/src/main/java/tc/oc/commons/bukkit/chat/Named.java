package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;

public interface Named {
    BaseComponent getStyledName(NameStyle style);
}
