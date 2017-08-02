package tc.oc.commons.bukkit.util;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionBrew;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Potion-related utilities. */
public class PotionUtils {

    public static Collection<PotionEffect> effects(PotionData data) {
        return Potion.getBrewer().getEffects(data.getType(), data.isUpgraded(), data.isExtended());
    }

    public static Collection<PotionEffect> effects(@Nullable PotionBrew potion, @Nullable Collection<PotionEffect> customEffects) {
        final ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        if(potion != null) {
            builder.addAll(potion.effects());
        }
        if(customEffects != null) {
            builder.addAll(customEffects);
        }
        return builder.build();
    }

    public static Collection<PotionEffect> effects(@Nullable PotionData potion, @Nullable Collection<PotionEffect> customEffects) {
        final ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        if(potion != null) {
            builder.addAll(effects(potion));
        }
        if(customEffects != null) {
            builder.addAll(customEffects);
        }
        return builder.build();
    }

    public static Collection<PotionEffect> effects(PotionMeta potion) {
        return effects(potion.getPotionBrew(), potion.getCustomEffects());
    }

    public static Collection<PotionEffect> effects(ItemStack potion) {
        return potion.getItemMeta() instanceof PotionMeta ? effects((PotionMeta) potion.getItemMeta())
                                                          : Collections.emptyList();
    }

    public static Collection<PotionEffect> effects(AreaEffectCloud cloud) {
        return effects(cloud.getBasePotionData(), cloud.getCustomEffects());
    }

    public static Collection<PotionEffect> effects(TippedArrow arrow) {
        return effects(arrow.getBasePotionData(), arrow.getCustomEffects());
    }

    public static @Nullable PotionEffectType primaryEffectType(ItemStack potion) {
        for(PotionEffect effect : effects(potion)) {
            return effect.getType();
        }
        return null;
    }
}
