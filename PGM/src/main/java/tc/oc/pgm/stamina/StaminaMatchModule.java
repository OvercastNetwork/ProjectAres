package tc.oc.pgm.stamina;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchScope;
import tc.oc.commons.bukkit.event.BlockPunchEvent;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.Repeatable;
import tc.oc.pgm.spawns.events.ParticipantDespawnEvent;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

@ListenerScope(MatchScope.LOADED)
public class StaminaMatchModule extends MatchModule implements Listener {

    public StaminaOptions getOptions() {
        return options;
    }

    private final StaminaOptions options;
    private final Map<Player, PlayerStaminaState> states = new HashMap<>();

    public StaminaMatchModule(Match match, StaminaOptions options) {
        super(match);
        this.options = options;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawn(ParticipantSpawnEvent event) {
        states.put(event.getPlayer().getBukkit(), new PlayerStaminaState(options, event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDespawn(ParticipantDespawnEvent event) {
        states.remove(event.getPlayer().getBukkit());
    }

    @Repeatable
    public void tick(Match match) {
        for(PlayerStaminaState state : states.values()) {
            state.tick();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!event.getFrom().toVector().equals(event.getTo().toVector())) {
            PlayerStaminaState state = states.get(event.getPlayer());
            if(state != null) state.onEvent(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSwing(PlayerAnimationEvent event) {
        PlayerStaminaState state = states.get(event.getPlayer());
        if(state != null) state.onEvent(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            PlayerStaminaState state = states.get(event.getEntity());
            if(state != null) state.onEvent(event);
        } else if(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
            PlayerStaminaState state = states.get(((EntityDamageByEntityEvent) event).getDamager());
            if(state != null) state.onEvent(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerShoot(ProjectileLaunchEvent event) {
        if(event.getEntity().getShooter() instanceof Player) {
            PlayerStaminaState state = states.get(event.getEntity().getShooter());
            if(state != null) state.onEvent(event);
        }
    }

    @EventHandler
    public void onPlayerPunchBlock(BlockPunchEvent event) {
        PlayerStaminaState state = states.get(event.getPlayer());
        if(state != null) state.onEvent(event);
    }

    @EventHandler
    public void onPlayerDig(BlockDamageEvent event) {
        PlayerStaminaState state = states.get(event.getPlayer());
        if(state != null) state.onEvent(event);
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        PlayerStaminaState state = states.get(event.getPlayer());
        if(state != null) state.onEvent(event);
    }
}
