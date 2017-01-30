package tc.oc.commons.bukkit.util;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nullable;

/**
 * Mechanism for retrieving basic state data about a match player
 * from a {@link Player}. All values are set by PGM using {@link MetadataValue}s.
 */
public interface PlayerStates {

    boolean isDead(Player player);

    default boolean isAlive(Player player) {
        return !isDead(player);
    }

    void setDead(Player player, @Nullable Boolean value);

    boolean isParticipating(Player player);

    default boolean isObserving(Player player) {
        return !isParticipating(player);
    }

    void setParticipating(Player player, @Nullable Boolean value);

}
