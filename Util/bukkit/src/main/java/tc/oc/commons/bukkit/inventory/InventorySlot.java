package tc.oc.commons.bukkit.inventory;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.core.util.Utils;

import static com.google.common.base.Preconditions.checkNotNull;

public class InventorySlot<I extends Inventory> {
    private final I inventory;
    private final Optional<Slot<I, ?>> slot;

    public InventorySlot(I inventory, Optional<Slot<I, ?>> slot) {
        this.inventory = checkNotNull(inventory);
        this.slot = checkNotNull(slot);
    }

    public InventorySlot(I inventory, Slot<I, ?> slot) {
        this(inventory, Optional.of(slot));
    }

    public InventorySlot(I inventory) {
        this(inventory, Optional.empty());
    }

    public static <I extends Inventory> InventorySlot<I> fromInventoryIndex(I inventory, int index) {
        final Slot slot = Slot.forInventoryIndex(inventory.getClass(), index);
        if(slot == null) {
            throw new IllegalArgumentException("Could not determine slot at index " + index + " in inventory " + inventory);
        }
        return new InventorySlot<>(inventory, slot);
    }

    public static <I extends Inventory> InventorySlot<I> fromInventoryIndex(I inventory, @Nullable Integer index) {
        return index != null ? fromInventoryIndex(inventory, (int) index)
                             : new InventorySlot<>(inventory);
    }

    public static InventorySlot<?> fromViewIndex(InventoryView view, int rawIndex) {
        final Slot slot = Slot.forViewIndex(view, rawIndex);
        if(slot == null) {
            throw new IllegalArgumentException("Could not determine slot at index " + rawIndex + " in view " + view);
        }
        final Inventory inventory = rawIndex == view.convertSlot(rawIndex) ? view.getTopInventory()
                                                                           : view.getBottomInventory();
        return new InventorySlot<>(inventory, slot);
    }

    public static InventorySlot<?> fromEvent(InventoryClickEvent event) {
        return fromViewIndex(event.getView(), event.getRawSlot());
    }

    public I inventory() {
        return inventory;
    }

    public Optional<Slot<I, ?>> slot() {
        return slot;
    }

    public void putItem(ItemStack item) {
        if(slot.isPresent()) {
            slot.get().putItem(inventory, item);
        } else {
            inventory.addItem(item);
        }
    }

    public @Nullable ItemStack getItem() {
        return slot.isPresent() ? slot.get().getItem(inventory)
                                : InventoryUtils.contents(inventory).findFirst().orElse(null);
    }

    public Optional<ItemStack> contents() {
        return slot.isPresent() ? Optional.ofNullable(slot.get().getItem(inventory))
                                : InventoryUtils.contents(inventory).findFirst();
    }

    @Override
    public int hashCode() {
        return Objects.hash(inventory, slot);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(InventorySlot.class, this, obj, that -> this.inventory.equals(that.inventory()) &&
                                                                    this.slot.equals(that.slot()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "{inventory=" + inventory +
               (slot.map(s -> " slot=" + s).orElse("")) +
               "}";
    }
}
