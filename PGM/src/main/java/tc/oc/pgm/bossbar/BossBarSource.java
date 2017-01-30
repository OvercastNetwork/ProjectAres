package tc.oc.pgm.bossbar;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

/**
 * A retained UI component that renders text and a health amount to the boss bar
 */
public interface BossBarSource {

    /**
     * Return the content to display to the given viewer, or empty to hide the bar for that viewer.
     *
     * This is the only method called by the underlying system. The default implementation
     * constructs a result from other methods in this interface.
     */
    default Optional<BossBarContent> barContent(Player viewer) {
        return isVisible(viewer) ? Optional.of(BossBarContent.of(barText(viewer), barProgress(viewer)))
                                 : Optional.empty();
    }

    /**
     * Is this bar currently visible? This is called before every render,
     * and if it returns false, the bar will be completely ignored.
     */
    default boolean isVisible(Player viewer) {
        return true;
    }

    /**
     * Called at render time to get the text to display in the bar.
     */
    default BaseComponent barText(Player viewer) {
        throw new UnsupportedOperationException();
    }

    /**
     * Called at render time to get the amount of health to display in the bar.
     * Valid range is 0 (no health) to 1 (full health).
     */
    default float barProgress(Player viewer) {
        throw new UnsupportedOperationException();
    }

    default BarColor barColor(Player viewer) {
        return BarColor.BLUE;
    }

    default BarStyle barStyle(Player viewer) {
        return BarStyle.SOLID;
    }

    default Set<BarFlag> barFlags(Player viewer) {
        return Collections.emptySet();
    }
}
