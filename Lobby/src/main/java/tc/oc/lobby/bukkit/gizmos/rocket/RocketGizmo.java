package tc.oc.lobby.bukkit.gizmos.rocket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import tc.oc.api.bukkit.users.Users;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.commons.core.chat.Component;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.GizmoConfig;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public class RocketGizmo extends Gizmo implements Listener {
    public final List<Rocket> rockets = Lists.newArrayList();
    public Map<Player, Integer> rocketedCount = Maps.newHashMap();

    public RocketGizmo(String name, String prefix, String description, Material icon, int cost) {
        super(name, prefix, description, icon, cost);
    }

    @Override
    public String getName(Player viewer) {
        return LobbyTranslations.get().t("gizmo.rocket.name", viewer);
    }

    @Override
    public String getDescription(Player viewer) {
        return LobbyTranslations.get().t("gizmo.rocket.description", viewer);
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Lobby.get(), new RocketTask(), 0, GizmoConfig.TICK_UPDATE_DELAY);
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        this.rocketedCount.remove(event.getPlayer());
    }

    @EventHandler
    public void entityDamage(final EntityDamageEvent event) {
        if(!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent realEvent = (EntityDamageByEntityEvent) event;
        if(!(realEvent.getDamager() instanceof Player) || !(realEvent.getEntity() instanceof Player)) return;

        final Player damager = (Player) realEvent.getDamager();
        final Player victim = (Player) realEvent.getEntity();

        if(victim.hasPermission(GizmoConfig.EXEMPT_PERMISSION)) return;

        if(!(Gizmos.gizmoMap.get(damager) instanceof RocketGizmo)) return;
        if(damager.getItemInHand().getType() != this.getIcon()) return;

        boolean cancel = false;
        for(Rocket rocket : this.rockets) {
            if(rocket.getObserver().equals(damager) && rocket.getVictim().equals(victim)) {
                cancel = true;
                break;
            }
        }
        if(cancel) return;

        List<Firework> fireworks = Lists.newArrayList();
        for(int i = 0; i < GizmoConfig.FIREWORK_COUNT; i++) {
            Firework firework = RocketUtils.getRandomFirework(victim.getLocation());
            firework.setVelocity(firework.getVelocity().multiply(new Vector(1, GizmoConfig.ROCKET_VELOCITY_MOD, 1)));
            fireworks.add(firework);
        }

        this.rockets.add(new Rocket(damager, victim, fireworks));

        RocketUtils.fakeDelta(damager, victim, new Vector(0, 3, 0));
        RocketUtils.takeOff(damager, victim.getLocation());

        Integer count = rocketedCount.get(damager);
        if(count == null) count = 0;

        count++;
        rocketedCount.put(damager, count);

        if(count % 10 == 0) {
            RaindropUtil.giveRaindrops(
                Users.playerId(damager), 1, null,
                new TranslatableComponent("gizmo.rocket.raindropsResult", new Component(String.valueOf(count), net.md_5.bungee.api.ChatColor.GOLD))
            );
        }

        event.setCancelled(true);
    }
}
