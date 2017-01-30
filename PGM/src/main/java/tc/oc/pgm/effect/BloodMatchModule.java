package tc.oc.pgm.effect;

import javax.inject.Inject;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.settings.SettingManagerProvider;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.settings.Settings;
import tc.oc.pgm.utils.EntityUtils;

/**
 * Render blood particle effects when a player is hurt.
 */
@ListenerScope(MatchScope.RUNNING)
public class BloodMatchModule extends MatchModule implements Listener {

    private final SettingManagerProvider settings;

    @Inject BloodMatchModule(SettingManagerProvider settings) {
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Location location = victim.getBoundingBox().center().toLocation(match.getWorld());
            if(event.getDamage() > 0 && location.getY() >= 0 && !victim.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                EntityUtils.entities(match.getWorld(), Player.class)
                     .filter(player -> settings.getManager(player).getValue(Settings.BLOOD, Boolean.class, false))
                     .forEach(player -> {
                         if(event instanceof EntityDamageByEntityEvent) {
                             player.playEffect(location, Effect.STEP_SOUND, Material.REDSTONE_WIRE);
                         } else {
                             player.playEffect(location, Effect.STEP_SOUND, Material.LAVA);
                         }
                     });
            }
        }
    }

}
