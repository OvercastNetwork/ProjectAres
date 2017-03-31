package tc.oc.pgm.kits;

import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.match.MatchPlayer;

public class SlotItemKit extends FreeItemKit {

    protected final @Inspect Slot.Player slot;

    public SlotItemKit(ItemStack item, Slot.Player slot) {
        super(item);
        this.slot = slot;
    }

    public Slot.Player slot() {
        return slot;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        items.put(slot, item, force);
    }

}
