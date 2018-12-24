package tc.oc.lobby.bukkit.gizmos.christmas.tree;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tc.oc.lobby.bukkit.Lobby;
import tc.oc.lobby.bukkit.gizmos.Gizmos;
import tc.oc.lobby.bukkit.gizmos.christmas.ChristmasGizmo;

public class ChristmasTreeGizmo extends ChristmasGizmo {

    public ChristmasTreeGizmo(String name, String prefix, String description, Material icon) {
        super(name, prefix, description, icon);
    }

    @Override protected void initialize() {
        Bukkit.getPluginManager().registerEvents(this, Lobby.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(Gizmos.gizmoMap.get(e.getPlayer()) instanceof ChristmasTreeGizmo)
            || e.getItem() == null || e.getItem().getType() != this.getIcon())
            return;

        Player player = e.getPlayer();
        Location loc = e.getPlayer().getLocation();

        new BukkitRunnable(){
            private double phi;
            private int i;

            @Override
            public void run() {
                phi += Math.PI/16;
                i++;

                for(double t = 0; t <= (2 * Math.PI); t += Math.PI/8){ // 16 iterations
                    for(double d = 0; d <= 1; d += 1){ // 2 iterations
                        double x = 0.3 * ((2 * Math.PI) - t) * 0.5 * StrictMath.cos(t + phi + (d * Math.PI));
                        double y = 0.5 * t;
                        double z = 0.3 * ((2 * Math.PI) - t) * 0.5 * StrictMath.sin(t + phi + (d * Math.PI));
                        loc.add(x,y,z);
                        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
                        loc.subtract(x,y,z);
                    }

                    for (double d = 0; d <= 1; d += 1) { // 2 iterations
                        double x = 0.2 * ((2 * Math.PI) - t) * 0.5 * StrictMath.cos(t + phi + (d * Math.PI));
                        double y = 0.45 * t;
                        double z = 0.2 * ((2 * Math.PI) - t) * 0.5 * StrictMath.sin(t + phi + (d * Math.PI));
                        loc.add(x, y, z);
                        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 1, 0, 0, 0, 0);
                        loc.subtract(x, y, z);
                    }
                }

                if ((i % 4) == 0) {
                    loc.add(0, 3.25, 0);
                    player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
                    loc.subtract(0, 3.25, 0);
                }
                if(phi > (10 * Math.PI)){
                    cancel();
                }

            }
        }.runTaskTimer(Lobby.get(), 0 , 1);
    }
}
