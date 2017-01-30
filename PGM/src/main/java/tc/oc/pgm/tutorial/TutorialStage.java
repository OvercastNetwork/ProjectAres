package tc.oc.pgm.tutorial;

import java.util.Collection;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.points.PointProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class TutorialStage {
    public TutorialStage(String title, Collection<String> messages, PointProvider teleportPoint) {
        this.title = checkNotNull(title, "title");
        this.messages = ImmutableList.copyOf(checkNotNull(messages, "messages"));
        this.teleportPoint = teleportPoint;
    }

    public String getTitle() {
        return this.title;
    }

    public ImmutableList<String> getMessages() {
        return this.messages;
    }

    public void sendMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("    " + ChatColor.YELLOW + ChatColor.BOLD + getTitle());
        getMessages().forEach(player::sendMessage);
        player.sendMessage(""); // blank line to separate from other chat spam
    }

    public void execute(MatchPlayer player) {
        checkNotNull(player, "player");

        this.sendMessage(player.getBukkit());

        if(this.teleportPoint != null) {
            Location teleport = this.getSafeTeleport(player);
            if(teleport != null) {
                player.getBukkit().teleport(teleport);
                player.getBukkit().setFlying(true);
            } else {
                player.sendMessage("    " + ChatColor.YELLOW + ChatColor.BOLD + PGMTranslations.t("tutorial.teleport.unsafe", player));
            }
            player.playSound(Sound.ENTITY_ENDERMEN_TELEPORT, 0.5f, 1.0f);
        } else {
            player.playSound(Sound.BLOCK_PISTON_EXTEND, 0.5f, 2.0f);
        }
    }

    static int SAFE_ITERATIONS = 10;

    @Nullable Location getSafeTeleport(MatchPlayer player) {
        if(this.teleportPoint == null) return null;

        Location safe = null;
        for(int i = 0; i < SAFE_ITERATIONS; i++) {
            safe = safeCheck(this.teleportPoint.getPoint(player.getMatch(), player.getBukkit()));
            if(safe != null) break;
        }
        return safe;
    }

    static Location safeCheck(Location location) {
        return isSafe(location) ? location : null;
    }

    static double STEVE_WIDTH = 0.6;

    static boolean isSafe(Location location) {
        Location scratch = location.clone();

        for(int level = 0; level <= 2; level++) {
            scratch.add(-STEVE_WIDTH/2, level, -STEVE_WIDTH/2); // set bottom left corner
            if(!isValidBlock(scratch)) return false;

            scratch.add(0, 0, STEVE_WIDTH); // set top left corner
            if(!isValidBlock(scratch)) return false;

            scratch.add(STEVE_WIDTH, 0, 0); // set top right corner
            if(!isValidBlock(scratch)) return false;

            scratch.add(0, 0, -STEVE_WIDTH); // set bottom right corner
            if(!isValidBlock(scratch)) return false;

            scratch = location.clone(); // reset
        }

        return true;
    }

    static boolean isValidBlock(Location location) {
        Block block = location.getBlock();
        if(block != null) {
            return block.getType() == Material.AIR;
        } else {
            return true;
        }
    }

    final String title;
    final ImmutableList<String> messages;
    final @Nullable PointProvider teleportPoint;
}
