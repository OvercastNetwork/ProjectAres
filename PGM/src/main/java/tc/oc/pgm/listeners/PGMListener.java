package tc.oc.pgm.listeners;

import java.util.logging.Logger;
import javax.inject.Inject;

import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.logging.Loggers;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.Config;
import tc.oc.pgm.events.MatchBeginEvent;
import tc.oc.pgm.events.MatchEndEvent;
import tc.oc.pgm.events.MatchLoadEvent;
import tc.oc.pgm.gamerules.GameRulesModule;
import tc.oc.pgm.match.MatchManager;
import tc.oc.pgm.modules.TimeLockModule;

/**
 * TODO: Break this down into more specific responsibilities
 */
public class PGMListener implements PluginFacet, Listener {

    private final Logger logger;
    private final MatchManager mm;

    @Inject PGMListener(Loggers loggers, MatchManager mm) {
        this.logger = loggers.get(getClass());
        this.mm = mm;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void kickAbandonedPlayers(final PlayerJoinEvent event) {
        // Spawn module should add player to a match at a lower priority.
        // If that hasn't happened for some reason, kick the player.
        if(mm.getPlayer(event.getPlayer()) == null) {
            event.getPlayer().kickPlayer(net.md_5.bungee.api.ChatColor.RED + Translations.get().t("incorrectWorld.kickMessage", event.getPlayer()));
            logger.severe("Kicking " + event.getPlayer().getName() + " because they failed to join a match");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void protect36(final PlayerInteractEvent event) {
        if(event.getClickedBlock() != null) {
            if(event.getClickedBlock().getType() == Material.PISTON_MOVING_PIECE) {
                event.setCancelled(true);
            }
        }
    }

    // sometimes arrows stuck in players persist through deaths
    @EventHandler
    public void fixStuckArrows(final PlayerRespawnEvent event) {
        event.getPlayer().setArrowsStuck(0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void clearActiveEnderPearls(final PlayerDeathEvent event) {
        for(Entity entity : event.getEntity().getWorld().getEntitiesByClass(EnderPearl.class)) {
            if(((EnderPearl) entity).getShooter() == event.getEntity()) {
                entity.remove();
            }
        }
    }

    // fix item pickup to work the way it should
    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleItemPickup(final PlayerPickupItemEvent event) {
        Player nearestPlayer = event.getPlayer();
        double closestDistance = event.getItem().getLocation().distance(event.getPlayer().getLocation());

        for(Entity nearEntity : event.getItem().getNearbyEntities(1.5, 1.5, 1.5)) {
            double distance = event.getItem().getLocation().distanceSquared(nearEntity.getLocation());

            if(nearEntity instanceof Player && distance < closestDistance) {
                nearestPlayer = (Player) nearEntity;
                closestDistance = distance;
            }
        }

        if(nearestPlayer != event.getPlayer()) event.setCancelled(true);
    }

    //
    // Time Lock
    // lock time before, during (if time lock enabled), and after the match
    //
    static final String DO_DAYLIGHT_CYCLE = "doDaylightCycle";

    @EventHandler
    public void lockTime(final MatchLoadEvent event) {
        event.getMatch().getWorld().setGameRuleValue(DO_DAYLIGHT_CYCLE, Boolean.toString(false));
    }

    @EventHandler
    public void unlockTime(final MatchBeginEvent event) {
        boolean unlockTime = false;
        if(!event.getMatch().getModuleContext().getModule(TimeLockModule.class).isTimeLocked()) {
            unlockTime = true;
        }

        GameRulesModule gameRulesModule = event.getMatch().getModuleContext().getModule(GameRulesModule.class);

        if (gameRulesModule != null && gameRulesModule.getGameRules().containsKey(DO_DAYLIGHT_CYCLE)) {
            unlockTime = Boolean.valueOf(gameRulesModule.getGameRules().get(DO_DAYLIGHT_CYCLE));
        }

        event.getMatch().getWorld().setGameRuleValue(DO_DAYLIGHT_CYCLE, Boolean.toString(unlockTime));
    }

    @EventHandler
    public void lockTime(final MatchEndEvent event) {
        event.getMatch().getWorld().setGameRuleValue(DO_DAYLIGHT_CYCLE, Boolean.toString(false));
    }

    @EventHandler
    public void nerfFishing(PlayerFishEvent event) {
        if (Config.Fishing.disableTreasure() && event.getCaught() instanceof Item) {
            Item caught = (Item) event.getCaught();
            if (caught.getItemStack().getType() != Material.RAW_FISH) {
                caught.setItemStack(new ItemStack(Material.RAW_FISH));
            }
        }
    }
}
