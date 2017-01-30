package tc.oc.pgm.itemkeep;

import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.inventory.Slot;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.query.MaterialQuery;

public class ItemKeepRules {
    public final Filter itemFilter, armorFilter;

    public ItemKeepRules(Filter itemFilter, Filter armorFilter) {
        this.itemFilter = itemFilter;
        this.armorFilter = armorFilter;
    }

    public boolean canKeepAny() {
        return !StaticFilter.DENY.equals(itemFilter) ||
               !StaticFilter.DENY.equals(armorFilter);
    }

    public boolean canKeep(Slot slot, ItemStack item) {
        final MaterialQuery query = MaterialQuery.of(item.getData());
        return itemFilter.query(query).toBoolean(false) ||
               (slot instanceof Slot.Armor &&
                armorFilter.query(query).toBoolean(false));
    }
}
