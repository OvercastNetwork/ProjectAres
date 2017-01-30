package tc.oc.pgm.filters;

import java.util.function.Predicate;
import javax.annotation.Nullable;

import org.bukkit.inventory.ImItemStack;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.kits.tag.ItemTags;

/**
 * Logic used by item filters
 */
public class ItemMatcher extends Inspectable.Impl implements Predicate<ItemStack> {

    @Inspect private final ImItemStack item;

    public ItemMatcher(ItemStack item) {
        this.item = normalize(item.clone()).immutableCopy();
    }

    @Override
    public boolean test(@Nullable ItemStack query) {
        if(query == null) return false;
        if(query.getType() != item.getType()) return false;

        query = normalize(query.clone());

        // Match if items stack, and query stack is at least big as the base stack
        return item.isSimilar(query) && query.getAmount() >= item.getAmount();
    }

    private static ItemStack normalize(ItemStack item) {
        // Ignore durability (if it's actually durability, and not data)
        if(item.getType().getMaxDurability() != 0) {
            item.setDurability((short) 0);
        }

        // Ignore these options
        ItemTags.PREVENT_SHARING.clear(item);
        ItemTags.LOCKED.clear(item);

        return item;
    }
}
