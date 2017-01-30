package tc.oc.pgm.filters.matcher.player;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.match.MatchPlayer;

public class HoldingItemFilter extends SpawnedPlayerItemFilter {
    public HoldingItemFilter(ItemStack base) {
        super(base);
    }

    @Override
    protected Iterable<ItemStack> getItems(MatchPlayer player) {
        final PlayerInventory inv = player.getBukkit().getInventory();
        return Arrays.asList(inv.getItemInMainHand(), inv.getItemInOffHand()); // List must allow nulls
    }
}
