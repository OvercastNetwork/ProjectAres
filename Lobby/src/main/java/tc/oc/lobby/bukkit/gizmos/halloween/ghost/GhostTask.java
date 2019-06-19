package tc.oc.lobby.bukkit.gizmos.halloween.ghost;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Skin;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import tc.oc.commons.bukkit.item.ItemBuilder;
import tc.oc.commons.bukkit.scheduler.RepeatingRunnable;
import tc.oc.commons.bukkit.util.Vectors;
import tc.oc.lobby.bukkit.Lobby;

public class GhostTask extends RepeatingRunnable {

  private static final Skin GHOST = new Skin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlc"
      + "y5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY3YTRmOTVlNWZlOTliNDViZTYxYmIzMzg4MmMxMmE5M2IyMmQyOTdmZDE3NjVhYjIxZTc3"
      + "NDhkYzZiOGNmMyJ9fX0=", null);
  private static final ItemStack HEAD = new ItemBuilder().material(Material.SKULL_ITEM).durability(3).skin(null, UUID.randomUUID(), GHOST).get();
  private static final ItemStack CHEST = new ItemBuilder().material(Material.LEATHER_CHESTPLATE).get();

  private static final int TOTAL_TICKS = 20 * 20;
  private static final int NUM_GHOSTS = 3;

  private final Player player;
  private final List<ArmorStand> ghosts;

  static {
    LeatherArmorMeta meta = (LeatherArmorMeta) CHEST.getItemMeta();
    meta.setColor(Color.BLACK);
    CHEST.setItemMeta(meta);
  }

  public GhostTask(Player player) {
    super(TOTAL_TICKS);
    this.player = player;
    this.ghosts = new ArrayList<>();
    for (int i = 0; i < NUM_GHOSTS; i++) {
      // proportion as to how far the ghost should be in the circle
      double prop = i / (double) NUM_GHOSTS;

      Vector v = new Vector(3, .5, 0);
      Vectors.rotateAroundAxisY(v, prop * 2 * Math.PI);

      Location spawnLoc = player.getLocation().add(v);
      spawnLoc.setDirection(player.getEyeLocation().subtract(spawnLoc.clone().add(0, player.getEyeHeight(), 0)).toVector());
      ghosts.add(createGhost(spawnLoc));
    }
  }

  @Override
  protected void repeat() {
    // end condition
    if (getIteration() == TOTAL_TICKS - 1) {
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, .5f, .75f);
      for (ArmorStand ghost : ghosts) {
        ghost.remove();
        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, ghost.getLocation().add(0, 1, 0), 1, 0, 0, 0, .5);
      }
    }

    // otherwise...
    for (int i = 0; i < NUM_GHOSTS; i++) {
      ArmorStand ghost = ghosts.get(i);

      // proportion as to how far the ghost should be in the circle
      double prop = i / (double) NUM_GHOSTS;

      // proportion as to how far in the animation we are
      double offset = getIteration() / (double) TOTAL_TICKS;

      Vector v = new Vector(3, .5, 0);
      Vectors.rotateAroundAxisY(v, prop * 2 * Math.PI + offset * 10);

      // put ghost in proper location
      Location nextLoc = player.getLocation().add(v);
      nextLoc.setDirection(player.getEyeLocation().subtract(nextLoc.clone().add(0, player.getEyeHeight(), 0)).toVector());
      ghost.teleport(nextLoc);

      // now lets spawn particles
      player.getWorld().spawnParticle(Particle.SMOKE_LARGE, ghost.getLocation().add(0, 1, 0), 3, 1, .3, 0, .1);
      if (getIteration() % 4 == 0) {
        player.getWorld().spawnParticle(Particle.FLAME, ghost.getLocation().add(0, 1, 0), 1, 0, .5, 0, .2);
      }
    }
  }

  @Override
  protected Plugin plugin() {
    return Lobby.get();
  }

  private ArmorStand createGhost(Location l) {
    ArmorStand stand = l.getWorld().spawn(l, ArmorStand.class);
    stand.setVisible(false);
    stand.setBasePlate(false);
    stand.setHelmet(HEAD);
    stand.setChestplate(CHEST);
    stand.setMarker(false);
    stand.setGravity(false);
    stand.setHeadPose(new EulerAngle(.4, 0, 0));

    return stand;
  }
}
