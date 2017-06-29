package tc.oc.pgm.kits;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.kits.tag.ItemTags;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import javax.inject.Inject;

@ListenerScope(MatchScope.RUNNING)
public class KitListener implements Listener {

    private final Match match;
    private final FeatureDefinitionContext context;

    @Inject KitListener(Match match, FeatureDefinitionContext context) {
        this.match = match;
        this.context = context;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getActor() instanceof Player && event.getEntity() instanceof Player) {
            Player actor = (Player)event.getActor();
            Player receiver = (Player)event.getEntity();
            ItemStack item = actor.getInventory().getItemInMainHand();
            String id = ItemTags.KIT.get(item);
            Kit kit = id == null ? null : context.get(id, Kit.class);
            if (kit != null) {
                kit.apply(match.getPlayer(receiver));
            }
            String hitterId = ItemTags.HITTER_KIT.get(item);
            Kit hitterKit = hitterId == null ? null : context.get(hitterId, Kit.class);
            if (hitterKit != null) {
                hitterKit.apply(match.getPlayer(actor));
            }
        }
    }
}
