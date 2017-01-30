package tc.oc.commons.bukkit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionBrew;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static tc.oc.commons.bukkit.util.PotionUtils.effects;

/**
 * Classification for potions that represents their harmfulness (or lack thereof). Not explicitly limited to {@link
 * org.bukkit.entity.ThrownPotion}s; other logical use cases include representation of {@link
 * org.bukkit.potion.PotionEffect}s and {@link org.bukkit.potion.PotionEffectType}s.
 */
public enum PotionClassification {
    /** Beneficial, has positive implications */
    BENEFICIAL(1),
    /** Unknown, positiveness of implications is not determinable */
    UNKNOWN(0),
    /** Harmful, has negative implications */
    HARMFUL(-1);

    private final int score;

    /** @param score Score of implications' positiveness */
    PotionClassification(final int score) {
        this.score = score;
    }

    /** An integer representation of this classification */
    public int getScore() {
        return this.score;
    }

    public PotionClassification inverse() {
        switch(this) {
            case BENEFICIAL: return HARMFUL;
            case HARMFUL: return BENEFICIAL;
            default: return this;
        }
    }

    private static final ImmutableSet<PotionEffectType> BUFFS = ImmutableSet.<PotionEffectType>builder()
        .add(PotionEffectType.ABSORPTION)
        .add(PotionEffectType.DAMAGE_RESISTANCE)
        .add(PotionEffectType.FAST_DIGGING)
        .add(PotionEffectType.FIRE_RESISTANCE)
        .add(PotionEffectType.HEAL)
        .add(PotionEffectType.HEALTH_BOOST)
        .add(PotionEffectType.INCREASE_DAMAGE)
        .add(PotionEffectType.INVISIBILITY)
        .add(PotionEffectType.JUMP)
        .add(PotionEffectType.LUCK)
        .add(PotionEffectType.NIGHT_VISION)
        .add(PotionEffectType.REGENERATION)
        .add(PotionEffectType.SATURATION)
        .add(PotionEffectType.SPEED)
        .add(PotionEffectType.WATER_BREATHING)
        .build();

    private static final ImmutableSet<PotionEffectType> BANES = ImmutableSet.<PotionEffectType>builder()
        .add(PotionEffectType.BLINDNESS)
        .add(PotionEffectType.CONFUSION)
        .add(PotionEffectType.HARM)
        .add(PotionEffectType.HUNGER)
        .add(PotionEffectType.POISON)
        .add(PotionEffectType.LEVITATION)
        .add(PotionEffectType.SLOW)
        .add(PotionEffectType.SLOW_DIGGING)
        .add(PotionEffectType.UNLUCK)
        .add(PotionEffectType.WEAKNESS)
        .add(PotionEffectType.WITHER)
        .build();

    private static final ImmutableMap<PotionEffectType, PotionClassification> CLASSIFICATIONS;

    static {
        final ImmutableMap.Builder<PotionEffectType, PotionClassification> builder = ImmutableMap.builder();
        for(PotionEffectType buff : PotionClassification.BUFFS) builder.put(buff, PotionClassification.BENEFICIAL);
        for(PotionEffectType buff : PotionClassification.BANES) builder.put(buff, PotionClassification.HARMFUL);
        CLASSIFICATIONS = builder.build();
    }

    /**
     * Scores the specified {@link PotionEffect}.
     *
     * @param effect The effect to score.
     * @return The score.
     */
    private static double getScore(final PotionEffect effect) throws IllegalArgumentException {
        return classify(effect.getType(), effect.getAmplifier()).getScore()
               * (effect.getAmplifier() < 0 ? -effect.getAmplifier() : effect.getAmplifier() + 1)
               * ((double) effect.getDuration()) / 20d;
    }

    /**
     * Scores the specified {@link PotionEffect}s.
     *
     * @param effects The potion effects to score.
     * @return The score.
     */
    private static double getScore(final Iterable<PotionEffect> effects) {
        double score = 0;
        for (PotionEffect effect : effects) {
            score += getScore(effect);
        }
        return score;
    }

    /**
     * Gets a classification for the given score. {@link #BENEFICIAL} if > 0, {@link #UNKNOWN} if == 0, {@link
     * #HARMFUL} if < 0.
     */
    private static PotionClassification fromScore(final double score) {
        return (score > 0.0 ? BENEFICIAL : (score == 0.0 ? UNKNOWN : HARMFUL));
    }

    public static PotionClassification classify(PotionEffectType type) {
        final PotionClassification c = CLASSIFICATIONS.get(type);
        return c == null ? UNKNOWN : c;
    }

    public static PotionClassification classify(PotionEffectType type, int level) {
        final PotionClassification c = classify(type);
        return level < 0 ? c.inverse() : c;
    }

    /**
     * Classifies the provided {@link PotionEffect}s as either {@link PotionClassification#BENEFICIAL}, {@link
     * PotionClassification#UNKNOWN}, or {@link PotionClassification#HARMFUL}.
     */
    public static PotionClassification classify(final Iterable<PotionEffect> effects) {
        return fromScore(getScore(effects));
    }

    public static PotionClassification classify(PotionData potion) {
        return classify(effects(potion));
    }

    public static PotionClassification classify(PotionBrew potion) {
        return classify(potion.effects());
    }

    public static PotionClassification classify(PotionMeta potion) {
        return classify(effects(potion));
    }

    public static PotionClassification classify(ItemStack potion) {
        return classify(effects(potion));
    }

    public static PotionClassification classify(final ThrownPotion potion) {
        return classify(potion.getEffects());
    }

    public static PotionClassification classify(AreaEffectCloud cloud) {
        return classify(effects(cloud));
    }

    public static PotionClassification classify(TippedArrow arrow) {
        return classify(effects(arrow));
    }
}
