package tc.oc.pgm.kits;

import java.util.stream.Stream;

import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.commons.core.util.Streams;

/**
 * Exists only for backward support of <clear-items/>
 */
@Deprecated
public class ClearItemsKit extends ClearKitBase {
    @Override
    protected Stream<Slot.Player> slots() {
        return Streams.concat(Slot.Storage.storage(),
                              Stream.of(Slot.OffHand.offHand()),
                              Stream.of(Slot.Cursor.cursor()));
    }

    @Override
    protected boolean filter(ItemStack item) {
        return true;
    }
}
