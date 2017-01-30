package tc.oc.pgm.loot;

import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.compose.Composition;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureInfo;
import tc.oc.pgm.xml.finder.Parent;

@FeatureInfo(name = "loot", plural = "lootables", singular = "loot")
public interface Loot extends FeatureDefinition {
    @Nodes(Parent.class)
    @Property Composition<ItemStack> items();
}
