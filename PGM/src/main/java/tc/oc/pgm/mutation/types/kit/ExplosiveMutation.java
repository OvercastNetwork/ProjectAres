package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.List;

public class ExplosiveMutation extends KitMutation {

    final static ItemKit TNT = new FreeItemKit(item(Material.TNT, 3));
    final static ItemKit LIGHTER = new FreeItemKit(new ItemStack(Material.FLINT_AND_STEEL, 1, (short)10));

    final static Range<Integer> RADIUS = Range.openClosed(0, 4);

    public ExplosiveMutation(Match match) {
        super(match, false);
        this.rewards.add(new KillReward(TNT));
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        PlayerInventory inv = player.getInventory();
        if(random().nextBoolean()) { // tnt and lighter kit
            if(!inv.contains(Material.TNT)) kits.add(TNT);
            if(!inv.contains(Material.FLINT_AND_STEEL)) kits.add(LIGHTER);
        }
    }

    @Override
    public void remove(MatchPlayer player) {
        super.remove(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setRadius(event.getRadius() + entropy().randomInt(RADIUS));
    }

}
