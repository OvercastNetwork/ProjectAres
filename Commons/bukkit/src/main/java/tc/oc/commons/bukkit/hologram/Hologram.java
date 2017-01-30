package tc.oc.commons.bukkit.hologram;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.bukkit.hologram.content.HologramContent;

/**
 * Represents a text-based in-game hologram, which may be animated.
 */
public interface Hologram {
    /**
     * Sets the current hologram content.
     *
     * @param plugin The plugin responsible for the hologram.
     * @param content The content to be displayed.
     */
    public void setContent(Plugin plugin, HologramContent content);

    /**
     * Displays the hologram to the specified player.
     *
     * @param player The player.
     * @throws java.lang.IllegalStateException If no content has been set.
     */
    public void show(Player player) throws IllegalStateException;

    /**
     * Hides the hologram from the specified player.
     *
     * @param player The player.
     */
    public void hide(Player player);

    public HologramContent getContent();
}
