package tc.oc.pgm.mutation.types.kit;

import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.mutation.types.KitMutation;

import java.util.List;

public class ExplosiveMutation extends KitMutation {

    final static ItemKit TNT = new FreeItemKit(item(Material.TNT, 8));
    final static ItemKit LIGHTER = new FreeItemKit(item(Material.FLINT_AND_STEEL));

    final static ItemKit FIRE_BOW = new FreeItemKit(new ItemBuilder(item(Material.BOW)).enchant(Enchantment.ARROW_FIRE, 1).get());
    final static ItemKit ARROWS = new FreeItemKit(item(Material.ARROW, 8));

    final static Range<Integer> RADIUS = Range.openClosed(0, 4);

    public ExplosiveMutation(Match match) {
        super(match, false);
        this.rewards.add(new KillReward(TNT));
        this.rewards.add(new KillReward(ARROWS));
    }

    @Override
    public void kits(MatchPlayer player, List<Kit> kits) {
        super.kits(player, kits);
        PlayerInventory inv = player.getInventory();
        if(random().nextBoolean()) { // tnt and lighter kit
            if(!inv.contains(Material.TNT)) kits.add(TNT);
            if(!inv.contains(Material.FLINT_AND_STEEL)) kits.add(LIGHTER);
        } else { // fire bow and arrows kit
            if(!inv.contains(Material.ARROW)) kits.add(ARROWS);
            if(!inv.contains(Material.BOW)) {
                kits.add(FIRE_BOW);
            } else {
                inv.all(Material.BOW).values().forEach(bow -> bow.addUnsafeEnchantments(FIRE_BOW.item().getEnchantments()));
            }
        }
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getInventory().all(Material.BOW).values().forEach(bow -> FIRE_BOW.item().getEnchantments().keySet().forEach(enchantment -> bow.removeEnchantment(enchantment)));
        super.remove(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setRadius(event.getRadius() + entropy().randomInt(RADIUS));
    }

}
