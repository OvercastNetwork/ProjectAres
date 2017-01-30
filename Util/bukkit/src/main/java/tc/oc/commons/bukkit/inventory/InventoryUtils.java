package tc.oc.commons.bukkit.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.item.ItemUtils;

public class InventoryUtils {
    private InventoryUtils() {}

    public static Inventory clickedInventory(InventoryClickEvent event) {
        return event.getSlot() == event.getRawSlot() ? event.getView().getTopInventory()
                                                     : event.getView().getBottomInventory();
    }

    public static <I extends Inventory, S extends Slot<I, ?>> Stream<? extends S> similar(I inv, Stream<? extends S> slots, ItemStack stack) {
        return slots.filter(slot -> stack.isSimilar(slot.getItem(inv)));
    }

    public static <I extends Inventory, S extends Slot<I, ?>> Stream<? extends S> empty(I inv, Stream<? extends S> slots) {
        return slots.filter(slot -> ItemUtils.isNothing(slot.getItem(inv)));
    }

    public static Stream<? extends Slot.Storage> similar(PlayerInventory inv, ItemStack stack) {
        return similar(inv, Slot.Storage.storage(), stack);
    }

    public static Stream<? extends Slot.Storage> empty(PlayerInventory inv) {
        return empty(inv, Slot.Storage.storage());
    }

    public static Stream<ItemStack> contents(Inventory inventory) {
        // Do this without calling getContents(), because it create a temp array
        final Stream.Builder<ItemStack> builder = Stream.builder();
        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);
            if(!ItemUtils.isNothing(item)) {
                builder.add(item);
            }
        }
        return builder.build();
    }

    public static <I extends Inventory, S extends Slot<I, ?>> Map<S, ItemStack> chooseSlots(I inv, Stream<? extends S> slots, ItemStack stack) {
        final Map<S, ItemStack> map = new HashMap<>();
        final ItemStack remaining = stack.clone();
        final List<? extends S> slotList = slots.collect(Collectors.toList());

        Stream.<S>concat(similar(inv, slotList.stream(), remaining), empty(inv, slotList.stream())).forEach(slot -> {
            if(!ItemUtils.isNothing(remaining)) {
                final int transferAmount = slot.maxTransferrableIn(remaining, inv);
                if(transferAmount > 0) {
                    final ItemStack transferStack = remaining.clone();
                    remaining.setAmount(remaining.getAmount() - transferAmount);
                    transferStack.setAmount(transferAmount);
                    map.put(slot, transferStack);
                }
            }
        });

        return map;
    }

    public static Map<Slot.Storage, ItemStack> chooseStorageSlots(PlayerInventory inv, ItemStack stack) {
        return chooseSlots(inv, Slot.Storage.storage(), stack);
    }
}
