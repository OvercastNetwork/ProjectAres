package tc.oc.pgm.kits;

import java.util.stream.Stream;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.match.MatchPlayer;

public abstract class ClearKitBase extends Kit.Impl {

    protected abstract Stream<Slot.Player> slots();

    protected abstract boolean filter(ItemStack item);

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        final PlayerInventory inv = player.getInventory();
        slots().forEach(slot -> {
            final ItemStack item = slot.getItem(inv);
            if(item != null && filter(item)) {
                slot.putItem(inv, null);
            }
        });
    }
}
