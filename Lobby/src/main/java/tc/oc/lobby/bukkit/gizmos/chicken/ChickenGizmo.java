package tc.oc.lobby.bukkit.gizmos.chicken;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.inject.Injector;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventRegistry;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.api.bukkit.users.Users;
import tc.oc.commons.bukkit.nick.IdentityProvider;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Component;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.LobbyTranslations;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.GizmoConfig;
import tc.oc.lobby.bukkit.gizmos.Gizmos;
import tc.oc.commons.bukkit.raindrops.RaindropUtil;

public class ChickenGizmo extends Gizmo implements Listener {

    private class Chicken implements Listener {
        final Player player;
        final NMSHacks.FakeChicken entity;
        final Set<Player> viewers = new HashSet<>();

        Chicken(Player player) {
            this.player = player;
            this.entity = new NMSHacks.FakeChicken(
                player.getWorld(),
                identityProvider.currentIdentity(player).getPublicName()
            );

            eventRegistry.registerListener(this);
        }

        boolean add(Player viewer) {
            if(viewers.add(viewer)) {
                viewer.hidePlayer(player);
                entity.spawn(viewer, player.getLocation());
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1f, 1f);
                viewer.playSound(viewer.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1f, 1f);
                return true;
            }

            return false;
        }

        @EventHandler
        void quit(PlayerQuitEvent event) {
            viewers.remove(event.getPlayer());
        }

        void move(PlayerMoveEvent event) {
            for(Player viewer : viewers) {
                entity.teleport(viewer, event.getTo());
            }
        }

        void destroy() {
            eventRegistry.unregisterListener(this);
            for(Player viewer : viewers) {
                entity.destroy(viewer);
            }
        }
    }

    private final Map<Player, Chicken> chickens = new HashMap<>();
    private final IdentityProvider identityProvider;
    private final EventRegistry eventRegistry;

    public Map<Player, Integer> chickendCount = Maps.newHashMap();

    public ChickenGizmo(String name, String prefix, String description, Material icon, int cost) {
        super(name, prefix, description, icon, cost);

        final Injector injector = Lobby.get().injector();
        identityProvider = injector.getInstance(IdentityProvider.class);
        eventRegistry = injector.getInstance(EventRegistry.class);
    }

    @Override
    public String getName(Player viewer) {
        return LobbyTranslations.get().t("gizmo.chicken.name", viewer);
    }

    @Override
    public String getDescription(Player viewer) {
        return LobbyTranslations.get().t("gizmo.chicken.description", viewer);
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void playerMove(PlayerMoveEvent event) {
        final Chicken chicken = chickens.get(event.getPlayer());
        if(chicken != null) chicken.move(event);
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        chickendCount.remove(event.getPlayer());
        final Chicken chicken = chickens.remove(event.getPlayer());
        if(chicken != null) chicken.destroy();
    }

    @EventHandler
    public void entityDamage(final EntityDamageEvent event) {
        if(!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent realEvent = (EntityDamageByEntityEvent) event;
        if(!(realEvent.getDamager() instanceof Player) || !(realEvent.getEntity() instanceof Player)) return;

        final Player damager = (Player) realEvent.getDamager();
        final Player victim = (Player) realEvent.getEntity();

        if(victim.hasPermission(GizmoConfig.EXEMPT_PERMISSION)) return;

        if(!(Gizmos.gizmoMap.get(damager) instanceof ChickenGizmo)) return;
        if(damager.getItemInHand().getType() != this.getIcon()) return;

        final Chicken chicken = chickens.computeIfAbsent(victim, Chicken::new);
        if(!chicken.add(damager)) return;

        chickendCount.compute(victim, (player, count) -> {
            count = count == null ? 0 : count + 1;
            if(count > 0 && count % 10 == 0) {
                RaindropUtil.giveRaindrops(
                    Users.playerId(damager), 1, null,
                    new TranslatableComponent("gizmo.chicken.raindropsResult", new Component(String.valueOf(count), net.md_5.bungee.api.ChatColor.GOLD))
                );
            }
            return count;
        });

        event.setCancelled(true);
    }
}
