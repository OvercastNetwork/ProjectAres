package tc.oc.pgm.filters.matcher.player;

import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.match.MatchPlayer;

public class CarryingItemFilter extends SpawnedPlayerItemFilter {
    public CarryingItemFilter(ItemStack base) {
        super(base);
    }

    @Override
    protected Iterable<ItemStack> getItems(MatchPlayer player) {
        return player.getBukkit().getInventory().contents();
    }
}
