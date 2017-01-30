package tc.oc.commons.bukkit.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import tc.oc.commons.bukkit.nick.Identity;

/**
 * Renders some part of a player's name, from {@link Identity}s and {@link NameType}s
 */
public interface PartialNameRenderer {

    /**
     * Get a legacy display name
     */
    String getLegacyName(Identity identity, NameType type);

    /**
     * Get a component display name
     */
    BaseComponent getComponentName(Identity identity, NameType type);
}
