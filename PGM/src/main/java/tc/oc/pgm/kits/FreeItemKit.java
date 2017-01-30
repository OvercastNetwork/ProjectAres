package tc.oc.pgm.kits;

import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.match.MatchPlayer;

public class FreeItemKit extends BaseItemKit {

    protected final ItemStack item;

    public FreeItemKit(ItemStack item) {
        this.item = item;
    }

    @Override
    public ItemStack item() {
        return item;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        items.add(item);
    }
}
