package tc.oc.pgm.kits;

import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.utils.MaterialMatcher;

/**
 * Clear items from the player's inventory
 */
public class ClearKit extends ClearKitBase {

    private final @Inspect Optional<Slot.Player> slot;
    private final @Inspect MaterialMatcher materials;

    public ClearKit(Optional<Slot.Player> slot, MaterialMatcher materials) {
        this.slot = slot;
        this.materials = materials;
    }

    @Override
    protected Stream<Slot.Player> slots() {
        return slot.map(Stream::of)
                   .orElseGet(Slot.Player::player);
    }

    @Override
    protected boolean filter(ItemStack item) {
        return materials.matches(item);
    }
}
