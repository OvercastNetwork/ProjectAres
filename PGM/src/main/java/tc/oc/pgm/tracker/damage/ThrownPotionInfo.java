package tc.oc.pgm.tracker.damage;

import java.util.Optional;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.bukkit.util.PotionUtils;
import tc.oc.pgm.match.ParticipantState;

public class ThrownPotionInfo extends EntityInfo implements PotionInfo {

    @Inspect private final PotionEffectType effectType;

    public ThrownPotionInfo(ThrownPotion entity, @Nullable ParticipantState owner) {
        super(entity, owner);
        this.effectType = PotionUtils.primaryEffectType(entity.getItem());
    }

    public ThrownPotionInfo(ThrownPotion entity) {
        this(entity, null);
    }

    @Override
    public Optional<PhysicalInfo> damager() {
        return Optional.of(this);
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return getOwner();
    }

    @Override
    public @Nullable PotionEffectType getPotionEffect() {
        return effectType;
    }

    @Override
    public BaseComponent getLocalizedName() {
        return new TranslatableComponent(NMSHacks.getTranslationKey(getPotionEffect()));
    }
}
