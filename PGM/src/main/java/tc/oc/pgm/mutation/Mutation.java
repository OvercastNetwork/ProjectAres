package tc.oc.pgm.mutation;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import tc.oc.api.docs.virtual.MatchDoc;
import tc.oc.commons.core.chat.Component;
import tc.oc.pgm.mutation.submodule.MutationModule;

import static tc.oc.pgm.mutation.submodule.MutationModules.*;

public enum Mutation {

    BLITZ       (null,               false),
    UHC         (null,               false),
    EXPLOSIVES  (Explosives.class,   true),
    NO_FALL     (null,               false),
    MOBS        (null,               false),
    STRENGTH    (Strength.class,     true),
    DOUBLE_JUMP (DoubleJump.class,   true),
    INVISIBILITY(Invisibility.class, true),
    LIGHTNING   (Lightning.class,    true),
    RAGE        (Rage.class,         true),
    ELYTRA      (Elytra.class,       true);

    public static final String TYPE_KEY = "mutation.type.";
    public static final String DESCRIPTION_KEY = ".desc";

    /**
     * The module class that handles this mutation.
     */
    private final @Nullable Class<? extends MutationModule> clazz;

    /**
     * Whether this mutation be changed during a match.
     */
    private final boolean change;

    Mutation(@Nullable Class<? extends MutationModule> clazz, boolean change) {
        this.clazz = clazz;
        this.change = change;
    }

    public static Mutation fromApi(MatchDoc.Mutation mutation) {
        return values()[mutation.ordinal()];
    }

    public MatchDoc.Mutation toApi() {
        return MatchDoc.Mutation.values()[ordinal()];
    }

    public Class<? extends MutationModule> getModuleClass() {
        return clazz;
    }

    public boolean isChangeable() {
        return change;
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
}
