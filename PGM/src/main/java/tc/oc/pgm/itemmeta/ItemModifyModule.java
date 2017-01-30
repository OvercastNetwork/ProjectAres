package tc.oc.pgm.itemmeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.ItemAttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.bukkit.item.BooleanItemTag;
import tc.oc.pgm.kits.ItemParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.MaterialMatcher;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "Item Modify")
public class ItemModifyModule implements MapModule, MatchModuleFactory<ItemModifyMatchModule> {
    private static final BooleanItemTag APPLIED = new BooleanItemTag("custom-meta-applied", false);

    private final List<ItemRule> rules;

    public ItemModifyModule(List<ItemRule> rules) {
        this.rules = rules;
    }

    public boolean shouldApply(ItemStack stack) {
        return stack != null &&
               stack.getType() != Material.AIR &&
               !APPLIED.get(stack);
    }

    public ItemStack applyToCopy(ItemStack stack) {
        if(shouldApply(stack)) {
            final boolean immutable = stack.isImmutable();
            stack = stack.clone();
            applyRules(stack);
            if(immutable) {
                stack = stack.immutableCopy();
            }
        }
        return stack;
    }

    public boolean applyRules(ItemStack stack) {
        if(!shouldApply(stack)) {
            return false;
        } else {
            boolean defaultAttributes = false;
            boolean attributesModified = false;

            for(ItemRule rule : rules) {
                if(rule.matches(stack)) {
                    rule.apply(stack);
                    APPLIED.set(stack, true);
                    attributesModified |= rule.meta.hasAttributeModifiers();
                    defaultAttributes |= rule.defaultAttributes;
                }
            }

            // If any rule had the defaultAttributes flag, and any custom attributes were added,
            // add the default attributes now. We do this here so it only happens once.
            if(defaultAttributes && attributesModified) {
                final ItemMeta meta = stack.getItemMeta();
                for(Map.Entry<String, List<ItemAttributeModifier>> entry : Bukkit.getItemFactory().getAttributeModifiers(stack.getData()).entrySet()) {
                    for(ItemAttributeModifier mod : entry.getValue()) {
                        meta.addAttributeModifier(entry.getKey(), mod);
                    }
                }
                stack.setItemMeta(meta);
            }

            return true;
        }
    }

    @Override
    public ItemModifyMatchModule createMatchModule(Match match) {
        return new ItemModifyMatchModule(match);
    }

    public static class Factory extends MapModuleFactory<ItemModifyModule> {
        @Override
        public @Nullable ItemModifyModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            List<ItemRule> rules = new ArrayList<>();
            for(Element elRule : XMLUtils.flattenElements(doc.getRootElement(), "item-mods", "rule")) {
                MaterialMatcher items = XMLUtils.parseMaterialMatcher(XMLUtils.getRequiredUniqueChild(elRule, "match"));

                // Always use a PotionMeta so the rule can have potion effects, though it will only apply those to potion items
                final Element elModify = XMLUtils.getRequiredUniqueChild(elRule, "modify");
                final PotionMeta meta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.POTION);
                context.needModule(ItemParser.class).parseItemMeta(elModify, meta);
                final boolean defaultAttributes = XMLUtils.parseBoolean(elModify.getAttribute("default-attributes"), false);

                ItemRule rule = new ItemRule(items, meta, defaultAttributes);
                rules.add(rule);
            }

            return rules.isEmpty() ? null : new ItemModifyModule(rules);
        }
    }
}
