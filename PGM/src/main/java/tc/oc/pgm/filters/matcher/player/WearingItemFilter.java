package tc.oc.pgm.filters.matcher.player;

import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.match.MatchPlayer;

public class WearingItemFilter extends SpawnedPlayerItemFilter {
    public WearingItemFilter(ItemStack base) {
        super(base);
    }

    @Override
    protected Iterable<ItemStack> getItems(MatchPlayer player) {
        return player.getBukkit().getInventory().armor();
    }
}
