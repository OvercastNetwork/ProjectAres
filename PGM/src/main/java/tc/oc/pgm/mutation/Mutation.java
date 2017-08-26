package tc.oc.pgm.mutation;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.PGM;
import tc.oc.pgm.mutation.types.MutationModule;
import tc.oc.pgm.mutation.types.kit.*;
import tc.oc.pgm.mutation.types.other.BlitzMutation;
import tc.oc.pgm.mutation.types.other.RageMutation;
import tc.oc.pgm.mutation.types.targetable.ApocalypseMutation;
import tc.oc.pgm.mutation.types.targetable.BomberMutation;
import tc.oc.pgm.mutation.types.targetable.LightningMutation;

import java.util.stream.Stream;

public enum Mutation {

    BLITZ      (BlitzMutation.class,       Material.IRON_FENCE, false),
    RAGE       (RageMutation.class,        Material.SKULL_ITEM, false),
    HARDCORE   (HardcoreMutation.class,    Material.GOLDEN_APPLE),
    JUMP       (JumpMutation.class,        Material.FEATHER),
    EXPLOSIVE  (ExplosiveMutation.class,   Material.FLINT_AND_STEEL),
    ELYTRA     (ElytraMutation.class,      Material.ELYTRA),
    PROJECTILE (ProjectileMutation.class,  Material.TIPPED_ARROW),
    ENCHANTMENT(EnchantmentMutation.class, Material.ENCHANTMENT_TABLE),
    POTION     (PotionMutation.class,      Material.POTION),
    EQUESTRIAN (EquestrianMutation.class,  Material.SADDLE),
    HEALTH     (HealthMutation.class,      Material.COOKED_BEEF),
    GLOW       (GlowMutation.class,        Material.GLOWSTONE_DUST, false),
    STEALTH    (StealthMutation.class,     Material.THIN_GLASS),
    ARMOR      (ArmorMutation.class,       Material.DIAMOND_CHESTPLATE),
    MOBS       (MobsMutation.class,        Material.MONSTER_EGG),
    LIGHTNING  (LightningMutation.class,   Material.JACK_O_LANTERN),
    BOMBER     (BomberMutation.class,      Material.TNT),
    BREAD      (BreadMutation.class,       Material.BREAD),
    BOAT       (BoatMutation.class,        Material.BOAT, false),
    TOOLS      (ToolsMutation.class,       Material.DIAMOND_PICKAXE),
    APOCALYPSE (ApocalypseMutation.class,  Material.NETHER_STAR);

    public static final String TYPE_KEY = "mutation.type.";
    public static final String DESCRIPTION_KEY = ".desc";

    private final @Nullable Class<? extends MutationModule> loader;
    private final Material guiDisplay;
    private final boolean pollable;

    Mutation(@Nullable Class<? extends MutationModule> loader, Material guiDisplay) {
        this(loader, guiDisplay, true);
    }

    Mutation(@Nullable Class<? extends MutationModule> loader, Material guiDisplay, boolean pollable) {
        this.loader = loader;
        this.guiDisplay = guiDisplay;
        this.pollable = pollable;
    }

    public Class<? extends MutationModule> loader() {
        return loader;
    }

    public Material getGuiDisplay() {
        return guiDisplay;
    }

    public boolean isPollable() {
        return pollable;
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
