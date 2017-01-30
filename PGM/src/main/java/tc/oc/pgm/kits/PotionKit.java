package tc.oc.pgm.kits;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tc.oc.pgm.match.MatchPlayer;

public class PotionKit extends Kit.Impl {
    protected final PotionEffect effect;

    public PotionKit(PotionEffect effect) {
        this.effect = effect;
    }

    private void applyEffect(MatchPlayer player, boolean force) {
        if(effect.getType().equals(PotionEffectType.HEALTH_BOOST)) {
            // Convert negative HB to max-health kit
            if(effect.getAmplifier() == -1 || effect.getDuration() == 0) {
                // Level 0 or zero-duration HB resets max health
                player.getBukkit().setMaxHealth(20);
                return;
            } else if(effect.getAmplifier() < -1 && effect.getDuration() == Integer.MAX_VALUE) {
                // Level < 0 HB with inf duration converts to a MH kit
                player.getBukkit().setMaxHealth(20 + (effect.getAmplifier() + 1) * 4);
                return;
            }
        }
        player.getBukkit().addPotionEffect(effect, force);
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        applyEffect(player, force);
        // No swirls by default, KitNode can re-enable them if it so desires
        player.getBukkit().setPotionParticles(false);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void remove(MatchPlayer player) {
        player.getBukkit().removePotionEffect(effect.getType());
    }
}
