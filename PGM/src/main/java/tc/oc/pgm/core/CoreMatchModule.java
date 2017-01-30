package tc.oc.pgm.core;

import java.util.List;
import javax.inject.Inject;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.util.BlockUtils;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.events.BlockTransformEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.ParticipantBlockTransformEvent;
import tc.oc.pgm.goals.events.GoalCompleteEvent;
import tc.oc.pgm.goals.events.GoalStatusChangeEvent;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.ParticipantState;

import static tc.oc.commons.core.stream.Collectors.toImmutableList;
import static tc.oc.pgm.map.ProtoVersions.MODES_IMPLEMENTATION_VERSION;


@ListenerScope(MatchScope.RUNNING)
public class CoreMatchModule extends MatchModule implements Listener {
    protected final List<Core> cores;
    protected int ccmTaskId1 = -1; // FIXME: hack
    protected int ccmTaskId2 = -1;

    @Inject private CoreMatchModule(Match match, List<CoreFactory> cores) {
        this.cores = cores.stream()
                          .map(def -> def.getGoal(match))
                          .collect(toImmutableList());
    }

    @Override
    public void enable() {
        if (this.match.getMapInfo().proto.isOlderThan(MODES_IMPLEMENTATION_VERSION)) {
            CoreConvertMonitor ccm = new CoreConvertMonitor(this);
            BukkitScheduler scheduler = this.match.getServer().getScheduler();
            this.ccmTaskId1 = scheduler.scheduleSyncDelayedTask(this.match.getPlugin(), ccm, 15*60*20); // 15 minutes
            this.ccmTaskId2 = scheduler.scheduleSyncDelayedTask(this.match.getPlugin(), ccm, 20*60*20); // 20 minutes
        }
    }

    @Override
    public void disable() {
        if (ccmTaskId1 != -1 || ccmTaskId2 != -1) {
            this.match.getServer().getScheduler().cancelTask(ccmTaskId1);
            this.match.getServer().getScheduler().cancelTask(ccmTaskId2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void leakCheck(final BlockTransformEvent event) {
        if(event.getWorld() != this.match.getWorld()) return;

        if(event.getNewState().getType() == Material.STATIONARY_LAVA) {
            Vector blockVector = BlockUtils.center(event.getNewState()).toVector();
            for(Core core : this.cores) {
                if(!core.hasLeaked() && core.getLeakRegion().contains(blockVector)) {
                    // core has leaked
                    core.markLeaked();
                    this.match.getPluginManager().callEvent(new CoreLeakEvent(this.match, core, event.getNewState()));
                    this.match.getPluginManager().callEvent(new GoalCompleteEvent(core,
                                                                                  true,
                                                                                  c -> false,
                                                                                  c -> !c.equals(core.getOwner()),
                                                                                  core.getContributions()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breakCheck(final BlockTransformEvent event) {
        if(event.getWorld() != this.match.getWorld()) return;
        ParticipantState player = ParticipantBlockTransformEvent.getPlayerState(event);

        Vector blockVector = BlockUtils.center(event.getNewState()).toVector();

        for(Core core : this.cores) {
            if(!core.hasLeaked() && core.getCasingRegion().contains(blockVector)) {
                if(event.getNewState().getType() == Material.AIR) {
                    if(player != null) {
                        Competitor team = player.getParty();

                        if(team == core.getOwner()) {
                            event.setCancelled(true, new TranslatableComponent("match.core.damageOwn"));
                        } else if (event.getOldState().getData().equals(core.getMaterial())) {
                            this.match.getPluginManager().callEvent(new CoreBlockBreakEvent(core, player, event.getOldState()));
                            core.touch(player);

                            // Note: team may not have touched a broken core if a different team broke it
                            if(!core.isCompleted(team) && !core.hasTouched(team)) {
                                this.match.getPluginManager().callEvent(new GoalStatusChangeEvent(core));
                            }
                        }
                    } else if(event.getCause() instanceof EntityExplodeEvent) {
                        // this is a temp fix until there is a tracker for placed minecarts (only dispensed are tracked right now)
                        if(((EntityExplodeEvent) event.getCause()).getEntity() instanceof ExplosiveMinecart) {
                            event.setCancelled(true);
                        }
                    } else if(event.getCause() instanceof BlockPistonRetractEvent) {
                        event.setCancelled(true);
                    }
                } else if(event.getCause() instanceof BlockPistonExtendEvent) {
                    event.setCancelled(true);
                } else if(event.getCause() instanceof BlockDispenseEvent) {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damageCheck(BlockDamageEvent event) {
        Block block = event.getBlock();
        if(block.getWorld() != this.match.getWorld()) return;
        MatchPlayer player = this.match.getPlayer(event.getPlayer());
        Vector center = BlockUtils.center(block).toVector();

        for(Core core : this.cores) {
            if(!core.hasLeaked() && core.getCasingRegion().contains(center) && player.getParty() == core.getOwner()) {
                event.setCancelled(true);
                player.sendWarning(PGMTranslations.t("match.core.damageOwn", player), true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void lavaProtection(final BlockTransformEvent event) {
        if(event.getWorld() != this.match.getWorld()) return;

        Vector blockVector = BlockUtils.center(event.getNewState()).toVector();
        for(Core core : this.cores) {
            if(core.getLavaRegion().contains(blockVector)) {
                event.setCancelled(true);
            }
        }
    }
}
