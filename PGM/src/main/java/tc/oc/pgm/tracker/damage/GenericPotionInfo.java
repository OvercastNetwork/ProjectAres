package tc.oc.pgm.tracker.damage;

import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.potion.PotionEffectType;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.inspect.Inspectable;
import tc.oc.pgm.match.ParticipantState;

public class GenericPotionInfo extends Inspectable.Impl implements PotionInfo {

    @Inspect private final PotionEffectType effectType;

    public GenericPotionInfo(PotionEffectType effectType) {
        this.effectType = effectType;
    }

    @Override
    public @Nullable PotionEffectType getPotionEffect() {
        return effectType;
    }

    @Override
    public String getIdentifier() {
        PotionEffectType effectType = getPotionEffect();
        return effectType != null ? effectType.getName() : "EMPTY";
    }

    @Override
    public BaseComponent getLocalizedName() {
        return new TranslatableComponent(NMSHacks.getTranslationKey(getPotionEffect()));
    }

    @Override
    public @Nullable ParticipantState getOwner() {
        return null;
    }

    @Override
    public @Nullable ParticipantState getAttacker() {
        return null;
    }
}
