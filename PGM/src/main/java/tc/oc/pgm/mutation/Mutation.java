package tc.oc.pgm.mutation;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.PGM;
import tc.oc.pgm.mutation.types.MutationModule;
import tc.oc.pgm.mutation.types.kit.ArmorMutation;
import tc.oc.pgm.mutation.types.kit.ElytraMutation;
import tc.oc.pgm.mutation.types.kit.EnchantmentMutation;
import tc.oc.pgm.mutation.types.kit.EquestrianMutation;
import tc.oc.pgm.mutation.types.kit.ExplosiveMutation;
import tc.oc.pgm.mutation.types.kit.GlowMutation;
import tc.oc.pgm.mutation.types.kit.HardcoreMutation;
import tc.oc.pgm.mutation.types.kit.HealthMutation;
import tc.oc.pgm.mutation.types.kit.JumpMutation;
import tc.oc.pgm.mutation.types.kit.MobsMutation;
import tc.oc.pgm.mutation.types.kit.PotionMutation;
import tc.oc.pgm.mutation.types.kit.ProjectileMutation;
import tc.oc.pgm.mutation.types.kit.StealthMutation;
import tc.oc.pgm.mutation.types.other.BlitzMutation;
import tc.oc.pgm.mutation.types.other.RageMutation;
import tc.oc.pgm.mutation.types.targetable.ApocalypseMutation;
import tc.oc.pgm.mutation.types.targetable.BomberMutation;
import tc.oc.pgm.mutation.types.targetable.LightningMutation;

import java.util.stream.Stream;

public enum Mutation {

    BLITZ      (BlitzMutation.class),
    RAGE       (RageMutation.class),
    HARDCORE   (HardcoreMutation.class),
    JUMP       (JumpMutation.class),
    EXPLOSIVE  (ExplosiveMutation.class),
    ELYTRA     (ElytraMutation.class),
    PROJECTILE (ProjectileMutation.class),
    ENCHANTMENT(EnchantmentMutation.class),
    POTION     (PotionMutation.class),
    EQUESTRIAN (EquestrianMutation.class),
    HEALTH     (HealthMutation.class),
    GLOW       (GlowMutation.class),
    STEALTH    (StealthMutation.class),
    ARMOR      (ArmorMutation.class),
    MOBS       (MobsMutation.class),
    LIGHTNING  (LightningMutation.class),
    BOMBER     (BomberMutation.class),
    APOCALYPSE (ApocalypseMutation.class);

    public static final String TYPE_KEY = "mutation.type.";
    public static final String DESCRIPTION_KEY = ".desc";

    private final @Nullable Class<? extends MutationModule> loader;

    Mutation(@Nullable Class<? extends MutationModule> loader) {
        this.loader = loader;
    }

    public Class<? extends MutationModule> loader() {
        return loader;
    }

    public String getName() {
        return TYPE_KEY + name().toLowerCase();
    }

    public String getDescription() {
        return getName() + DESCRIPTION_KEY;
    }

    public BaseComponent getComponent(ChatColor color) {
        return new Component(new TranslatableComponent(getName()), color).hoverEvent(HoverEvent.Action.SHOW_TEXT, new Component(new TranslatableComponent(getDescription()), ChatColor.GRAY));
    }

    public static Function<Mutation, BaseComponent> toComponent(final ChatColor color) {
        return mutation -> mutation.getComponent(color);
    }

    public static Stream<Mutation> fromString(final String name) {
        try {
            return Stream.of(Mutation.valueOf(name));
        } catch(IllegalArgumentException iae) {
            PGM.get().getLogger().warning("Unable to find mutation named '" + name + "'");
            return Stream.empty();
        }
    }

}
