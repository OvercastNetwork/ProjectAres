package tc.oc.pgm.blockdrops;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.util.Pair;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.itemmeta.ItemModifyModule;
import tc.oc.pgm.kits.ItemParser;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.modules.InfoModule;
import tc.oc.pgm.regions.EverywhereRegion;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "Custom Block Drops", requires = { InfoModule.class })
public class BlockDropsModule implements MapModule, MatchModuleFactory<BlockDropsMatchModule> {
    private final BlockDropsRuleSet ruleSet;

    public BlockDropsModule(BlockDropsRuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    @Override
    public BlockDropsMatchModule createMatchModule(Match match) {
        return new BlockDropsMatchModule(match, this.ruleSet);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------
    @SuppressWarnings("deprecation")
    public static BlockDropsModule parse(MapModuleContext context, Logger logger, Document doc)
        throws InvalidXMLException {
        final List<BlockDropsRule> rules = new ArrayList<>();
        final FilterParser filterParser = context.needModule(FilterParser.class);
        final RegionParser regionParser = context.needModule(RegionParser.class);
        final KitParser kitParser = context.needModule(KitParser.class);
        final ItemParser itemParser = context.needModule(ItemParser.class);
        final Optional<ItemModifyModule> itemModifier = context.module(ItemModifyModule.class);

        for(Element elRule : XMLUtils.flattenElements(doc.getRootElement(), ImmutableSet.of("block-drops", "blockdrops"), ImmutableSet.of("rule"))) {
            Filter filter = filterParser.parseOptionalProperty(elRule, "filter").orElse(null);
            Region region = regionParser.property(elRule).optionalUnion(EverywhereRegion.INSTANCE);
            Kit kit = kitParser.property(elRule, "kit").optional(null);
            boolean dropOnWrongTool = XMLUtils.parseBoolean(Node.fromChildOrAttr(elRule, "wrong-tool", "wrongtool"), false);
            boolean punchable = XMLUtils.parseBoolean(Node.fromChildOrAttr(elRule, "punch"), false);
            boolean trample = XMLUtils.parseBoolean(Node.fromChildOrAttr(elRule, "trample"), false);
            Float fallChance = XMLUtils.parseNumber(Node.fromChildOrAttr(elRule, "fall-chance"), Float.class, (Float) null);
            Float landChance = XMLUtils.parseNumber(Node.fromChildOrAttr(elRule, "land-chance"), Float.class, (Float) null);
            Double fallSpeed = XMLUtils.parseNumber(Node.fromChildOrAttr(elRule, "fall-speed"), Double.class, (Double) null);

            MaterialData replacement = null;
            if(elRule.getChild("replacement") != null) {
                replacement = XMLUtils.parseBlockMaterialData(Node.fromChildOrAttr(elRule, "replacement"));
            }

            int experience = XMLUtils.parseNumber(Node.fromChildOrAttr(elRule, "experience"), Integer.class, 0);


            List<Pair<Double, ItemStack>> items = new ArrayList<>();
            for(Element elDrops : elRule.getChildren("drops")) {
                for(Element elItem : elDrops.getChildren("item")) {
                    final ItemStack item = itemParser.parseItem(elItem, false);
                    itemModifier.ifPresent(imm -> imm.applyRules(item));
                    items.add(Pair.create(XMLUtils.parseNumber(elItem.getAttribute("chance"), Double.class, 1d),
                                          item.immutableCopy()));
                }
            }

            rules.add(new BlockDropsRule(filter, region, dropOnWrongTool, punchable, trample, new BlockDrops(items, kit, experience, replacement, fallChance, landChance, fallSpeed)));
        }

        // BlockDropsModule must always be loaded, even if there are no rules defined,
        // otherwise modules that depend on it e.g. DestroyablesModule will be silently
        // skipped by the module loader. We need better module dependency logic.
        return new BlockDropsModule(new BlockDropsRuleSet(rules));
    }
}
