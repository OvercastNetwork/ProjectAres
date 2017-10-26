package tc.oc.lobby.bukkit.gizmos.halloween;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.gizmos.Gizmo;
import tc.oc.lobby.bukkit.gizmos.Gizmos;

public class HeadlessHorsemanGizmo extends Gizmo implements Listener {
    private Map<Player, HeadlessHorseman> mutated;
    private Map<Player, HeadlessHorse> horseByPlayer;

    public HeadlessHorsemanGizmo(String name, String prefix, String description, Material icon, int cost) {
        super(name, prefix, description, icon, cost);
        this.mutated = new HashMap<>();
        this.horseByPlayer = new HashMap<>();
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getAction() == Action.PHYSICAL
            || !(Gizmos.gizmoMap.get(e.getPlayer()) instanceof HeadlessHorsemanGizmo)
            || e.getItem() == null || e.getItem().getType() != this.getIcon()) return;

        final Player player = e.getPlayer();
        if(mutated.get(player) == null) {
            HeadlessHorseman horseman = new HeadlessHorseman(player);
            mutated.put(player, horseman);
            horseByPlayer.put(player, horseman.getHeadlessHorse());
            createEffect(player);
        } else {
            mutated.get(player).restore();
            mutated.remove(player);
            horseByPlayer.remove(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        mutated.remove(player);
        horseByPlayer.get(player).despawn();
        horseByPlayer.remove(player);
    }

    private void createEffect(Player viewer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Location location = viewer.getLocation();
                if (mutated.get(viewer) == null || horseByPlayer.get(viewer) == null) cancel();
                for(int i = 0; i < 360; i++) {
                    double radians = Math.toRadians(i);
                    double x = Math.cos(radians);
                    double z = Math.sin(radians);
                    location.add(x, 2.75, z);
                    location.getWorld().playEffect(location, Effect.SMOKE, 2);
                    location.subtract(x, 2.75, z);
                }
            }
        }.runTaskTimer(Lobby.get(), 1 * 20, 5 * 20);
    }
}
