package tc.oc.pgm.kits;

import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.kits.tag.Grenade;
import tc.oc.pgm.match.MatchScope;

@ListenerScope(MatchScope.RUNNING)
public class GrenadeListener implements Listener {

    private final Plugin plugin;

    @Inject GrenadeListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGrenadeLaunch(final ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            ItemStack stack = player.getItemInHand();

            if(stack != null) {
                // special case for grenade arrows
                if (stack.getType() == Material.BOW) {
                    int arrows = player.getInventory().first(Material.ARROW);
                    if (arrows == -1) return;
                    stack = player.getInventory().getItem(arrows);
                }

                Grenade grenade = Grenade.ITEM_TAG.get(stack);
                if(grenade != null) {
                    grenade.set(plugin, event.getEntity());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGrenadeExplode(final ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Grenade grenade = Grenade.get(event.getEntity());
            if(grenade != null) {
                NMSHacks.createExplosion(event.getEntity(), event.getEntity().getLocation(), grenade.power, grenade.fire, grenade.destroy);
                event.getEntity().remove();
            }
        }
    }
}
