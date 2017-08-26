package tc.oc.pgm.kits;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.match.MatchPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        int left = item.getAmount();
        PlayerInventory inv = player.getInventory();
        for(Map.Entry<Slot.Player, Optional<ItemStack>> entry : Slot.Player.player()
               .collect(Collectors.toMap(Function.identity(), slot -> slot.item(inv))).entrySet()) {
            Slot.Player slot = entry.getKey();
            Optional<ItemStack> itemMaybe = entry.getValue();
            if(itemMaybe.isPresent() && this.item.isSimilar(itemMaybe.get())) {
                ItemStack item = itemMaybe.get();
                int delta = item.getAmount() - left;
                if(delta > 0) {
                    ItemStack replaced = item.clone();
                    replaced.setAmount(delta);
                    slot.putItem(inv, replaced);
                    break;
                } else {
                    slot.putItem(inv, null);
                    if(delta < 0) {
                        left = -delta;
                    }
                }
            }
        }
    }

}
