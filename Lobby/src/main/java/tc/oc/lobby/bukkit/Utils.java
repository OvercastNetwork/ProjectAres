package tc.oc.lobby.bukkit;

import java.util.List;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.PacketPlayOutScoreboardScore;
import net.minecraft.server.ScoreboardObjective;
import net.minecraft.server.ScoreboardScore;
import net.minecraft.server.ScoreboardServer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.scoreboard.CraftObjective;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import tc.oc.commons.bukkit.item.ItemBuilder;

public class Utils {
    public static void displayScoreboard(Player player, Objective objective) {
        ScoreboardServer server = (ScoreboardServer) ((CraftScoreboard) objective.getScoreboard()).getHandle();
        ScoreboardObjective nmsObjective = ((CraftObjective) objective).getHandle();
        List<Packet<?>> packets = server.getScoreboardScorePacketsForObjective(nmsObjective);

        for(Packet packet : packets) {
            sendPacket(player, packet);
        }

        sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, nmsObjective));
    }

    public static void removeScore(Player player, String score) {
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(score);
        sendPacket(player, packet);
    }

    public static void addScore(Player player, Objective objective, String name, int value) {
        ScoreboardScore score = new ScoreboardScore(((CraftScoreboard)objective.getScoreboard()).getHandle(), ((CraftObjective) objective).getHandle() , name);
        score.setScore(value);
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(score);

        sendPacket(player, packet);
    }

    private static void sendPacket(Player bukkitPlayer, Packet packet) {
        EntityPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        nmsPlayer.playerConnection.sendPacket(packet);
    }

    public static void resetPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setChestplate(new ItemBuilder().material(Material.ELYTRA).unbreakable(true).get());
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(player.hasPermission("lobby.fly"));
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setGravity(true);
        player.setPotionParticles(false);
        player.hideTitle();
        player.setCollidesWithEntities(false);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.5, 0.5, 0.5, 0);
    }

    public static ItemStack getGhastTear(Player player, int count) {
        ItemStack raindrops = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = raindrops.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Droplets" + ChatColor.DARK_PURPLE + " | " + ChatColor.WHITE + String.format("%,d", count));
        raindrops.setItemMeta(meta);

        return raindrops;
    }

    public static void giveGhastTear(Player player, int count) {
        player.getInventory().remove(Material.GHAST_TEAR);
        player.getInventory().setItem(4, getGhastTear(player, count));
    }
}
