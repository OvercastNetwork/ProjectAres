package tc.oc.pgm.kits;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import tc.oc.pgm.match.MatchPlayer;

/**
 * Disowns any Ender Pearls the player has thrown
 */
public class ResetEnderPearlsKit extends Kit.Impl {
    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        Player bukkitPlayer = player.getBukkit();
        for(EnderPearl pearl : bukkitPlayer.getWorld().getEntitiesByClass(EnderPearl.class)) {
            if(pearl.getShooter() == bukkitPlayer) {
                pearl.setShooter(null);
            }
        }
    }
}
