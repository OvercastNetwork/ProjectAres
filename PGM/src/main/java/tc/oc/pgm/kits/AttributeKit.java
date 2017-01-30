package tc.oc.pgm.kits;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.match.MatchPlayer;

public class AttributeKit extends Kit.Impl {

    @Inspect private final Attribute attribute;
    @Inspect private final AttributeModifier modifier;

    public AttributeKit(Pair<Attribute, AttributeModifier> pair) {
        this(pair.first, pair.second);
    }

    public AttributeKit(Attribute attribute, AttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public void apply(MatchPlayer player, boolean force, ItemKitApplicator items) {
        player.facet(AttributePlayerFacet.class)
              .addModifier(attribute, modifier);
    }

    @Override
    public void remove(MatchPlayer player) {
        player.facet(AttributePlayerFacet.class)
              .removeModifier(attribute, modifier);
    }
}
