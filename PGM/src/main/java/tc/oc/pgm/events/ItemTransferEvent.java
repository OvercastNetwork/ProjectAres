package tc.oc.pgm.events;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.event.GeneralizingEvent;
import tc.oc.commons.bukkit.inventory.InventorySlot;

/**
 * Fired when an item moves in/out of an Inventory
 */
public class ItemTransferEvent extends GeneralizingEvent {
    public enum Type {
        PLACE,      // Item placed in an inventory through a GUI
        TAKE,       // Item taken from an inventory through a GUI
        TRANSFER,   // Item transferred instantly from one inventory to another
        PICKUP,     // Item picked up from the world
        DROP,       // Item dropped into the world
        PLUGIN      // Item transferred somehow by a plugin
    }

    protected final Type type;
    protected final Optional<InventorySlot<?>> from;
    protected final Optional<InventorySlot<?>> to;
    protected final ItemStack itemStack;
    @Nullable protected final Item itemEntity;
    protected int quantity;

    public ItemTransferEvent(@Nullable Event cause,
                             Type type,
                             Optional<InventorySlot<?>> from,
                             Optional<InventorySlot<?>> to,
                             ItemStack itemStack,
                             @Nullable Item itemEntity,
                             int quantity) {
        super(cause);
        this.type = type;
        this.from = from;
        this.to = to;
        this.itemStack = itemStack;
        this.itemEntity = itemEntity;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        String s = this.getClass().getName() +
                   " cause=" + this.cause.getEventName() +
                   " type=" + this.type;

        if(from.isPresent()) {
            s += " from=" + from.get();
        }

        if(to.isPresent()) {
            s += " to=" + to.get();
        }

        if(this.itemStack != null) {
            s += " stack=" + this.itemStack;
        }

        if(this.itemEntity != null) {
            s += " entity=" + this.itemEntity;
        }

        return s + " qty=" + this.quantity;
    }

    public Type getType() {
        return type;
    }

    public Optional<InventorySlot<?>> from() {
        return from;
    }

    public Optional<InventorySlot<?>> to() {
        return to;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Nullable
    public Item getItemEntity() {
        return itemEntity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    private static final HandlerList handlers = new HandlerList();
    @Override public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
