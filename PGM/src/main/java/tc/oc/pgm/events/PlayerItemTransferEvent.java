package tc.oc.pgm.events;

import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.InventorySlot;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerItemTransferEvent extends ItemTransferEvent {
    private final Player player;
    @Nullable protected final ItemStack cursorItems;

    public PlayerItemTransferEvent(Event cause,
                                   Type type,
                                   Player player,
                                   Optional<InventorySlot<?>> from,
                                   Optional<InventorySlot<?>> to,
                                   ItemStack itemStack,
                                   @Nullable Item itemEntity,
                                   int quantity,
                                   @Nullable ItemStack cursorItems) {

        super(cause, type, from, to, itemStack, itemEntity, quantity);
        this.player = checkNotNull(player);
        this.cursorItems = cursorItems;
    }

    @Override
    public String toString() {
        String s = super.toString();

        s += " player=" + this.player.getName();

        if(this.cursorItems != null) {
            s += " cursor=" + this.cursorItems;
        }

        return s;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public Player getActor() {
        return getPlayer();
    }

    @Nullable
    public ItemStack getCursorItems() {
        return cursorItems;
    }

    /**
     * Return the quantity of items stackable with the given item that
     * the player was in posession of prior to the transfer event. This
     * includes any items being carried on the cursor.
     */
    public int getPriorQuantity(ItemStack type) {
        int quantity = 0;
        for(ItemStack stack : this.player.getInventory().contents()) {
            if(stack != null && stack.isSimilar(type)) {
                quantity += stack.getAmount();
            }
        }
        if(this.cursorItems != null && this.cursorItems.isSimilar(type)) {
            quantity += this.cursorItems.getAmount();
        }
        return quantity;
    }

    /**
     * Equivalent to getPriorQuantity(getItemStack())
     */
    public int getPriorQuantity() {
        return this.getPriorQuantity(this.itemStack);
    }

    public boolean isFromPlayer() {
        return from.isPresent() && player.equals(from.get().inventory().getHolder());
    }

    public boolean isToPlayer() {
        return to.isPresent() && player.equals(to.get().inventory().getHolder());
    }

    public boolean isAcquiring() {
        return type == Type.TAKE || (!isFromPlayer() && isToPlayer());
    }

    public boolean isRelinquishing() {
        return type == Type.PLACE || (isFromPlayer() && !isToPlayer());
    }
}
