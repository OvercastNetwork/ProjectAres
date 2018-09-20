package tc.oc.lobby.bukkit.gizmos.halloween.horse;

import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PoseFlag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tc.oc.commons.bukkit.chat.WarningComponent;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.gizmos.Gizmos;
import tc.oc.lobby.bukkit.gizmos.halloween.HalloweenGizmo;
import tc.oc.minecraft.protocol.MinecraftVersion;

public class HeadlessHorsemanGizmo extends HalloweenGizmo implements Listener {
    private Map<Player, HeadlessHorseman> mutated;
    private Map<Player, HeadlessHorse> horseByPlayer;

    public HeadlessHorsemanGizmo(String name, String prefix, String description, Material icon) {
        super(name, prefix, description, icon);
        this.mutated = new WeakHashMap<>();
        this.horseByPlayer = new WeakHashMap<>();
    }

    @Override
    protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(!(Gizmos.gizmoMap.get(e.getPlayer()) instanceof HeadlessHorsemanGizmo)
            || e.getItem() == null || e.getItem().getType() != this.getIcon()) return;

        final Player player = e.getPlayer();
        if(mutated.get(player) == null) {
            if (MinecraftVersion.atLeast(MinecraftVersion.MINECRAFT_1_9, player.getProtocolVersion())) {
                HeadlessHorseman horseman = new HeadlessHorseman(player);
                mutated.put(player, horseman);
                horseByPlayer.put(player, horseman.getHeadlessHorse());
                createEffect(player);
            } else {
                player.sendMessage(new WarningComponent("version.too.old.gizmo"));
            }
        } else {
            mutated.get(player).restore();
            mutated.remove(player);
            horseByPlayer.remove(player);
        }
    }

    @EventHandler
    public void onDismount(PlayerMoveEvent event) {
      if (event.getEntityTo().poseFlags().contains(PoseFlag.RIDING)) return;
      if (!mutated.containsKey(event.getPlayer())) return;

      mutated.get(event.getPlayer()).restore();
      mutated.remove(event.getPlayer());
      horseByPlayer.remove(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!mutated.containsKey(event.getPlayer())) return;

        mutated.get(event.getPlayer()).restore();
        mutated.remove(event.getPlayer());
        horseByPlayer.remove(event.getPlayer());
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
