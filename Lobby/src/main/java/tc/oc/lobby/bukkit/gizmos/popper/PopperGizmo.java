package tc.oc.lobby.bukkit.gizmos.popper;

import com.google.common.collect.Maps;
import java.util.Map;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.users.Users;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;
import tc.oc.commons.core.chat.Component;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.GizmoConfig;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public class PopperGizmo extends Gizmo implements Listener {
    public Map<Player, Integer> poppedCount = Maps.newHashMap();

    public PopperGizmo(String name, String prefix, String description, Material icon, int cost) {
        super(name, prefix, description, icon, cost);
    }

    @Override
    public String getName(Player viewer) {
        return LobbyTranslations.get().t("gizmo.popper.name", viewer);
    }

    @Override
    public String getDescription(Player viewer) {
        return LobbyTranslations.get().t("gizmo.popper.description", viewer);
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        this.poppedCount.remove(event.getPlayer());
    }

    @EventHandler
    public void entityDamage(final EntityDamageEvent event) {
        if(!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent realEvent = (EntityDamageByEntityEvent) event;
        if(!(realEvent.getDamager() instanceof Player) || !(realEvent.getEntity() instanceof Player)) return;

        final Player damager = (Player) realEvent.getDamager();
        final Player victim = (Player) realEvent.getEntity();

        if(victim.hasPermission(GizmoConfig.EXEMPT_PERMISSION)) return;

        if(!(Gizmos.gizmoMap.get(damager) instanceof PopperGizmo)) return;
        if(damager.getItemInHand().getType() != this.getIcon()) return;

        if(!damager.canSee(victim)) return;

        damager.hidePlayer(victim);
        damager.playSound(damager.getLocation(), Sound.BLOCK_LAVA_POP, 1f, 2f);

        Integer count = poppedCount.get(damager);
        if(count == null) count = 0;

        count++;
        poppedCount.put(damager, count);

        if(count % 10 == 0) {
            RaindropUtil.giveRaindrops(
                Users.playerId(damager), 1, null,
                new TranslatableComponent("gizmo.popper.raindropsResult", new Component(String.valueOf(count), net.md_5.bungee.api.ChatColor.GOLD))
            );
        }

        event.setCancelled(true);
    }
}
