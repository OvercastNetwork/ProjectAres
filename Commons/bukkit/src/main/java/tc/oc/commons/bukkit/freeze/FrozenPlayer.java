package tc.oc.commons.bukkit.freeze;

/**
 * Handle for a {@link org.bukkit.entity.Player} frozen by the {@link PlayerFreezer}.
 *
 * Multiple handles can exist for the same player simultaneously. The player remains
 * frozen until ALL handles are thawed.
 */
public interface FrozenPlayer {
    void thaw();
}
