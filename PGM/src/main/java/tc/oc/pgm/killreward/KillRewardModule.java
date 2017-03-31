package tc.oc.pgm.killreward;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.bukkit.inventory.ItemStack;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.itemmeta.ItemModifyModule;
import tc.oc.pgm.kits.ItemParser;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitNode;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name="Kill Reward")
public class KillRewardModule implements MapModule, MatchModuleFactory<KillRewardMatchModule> {
    protected final List<KillReward> rewards;

    public KillRewardModule(List<KillReward> rewards) {
        this.rewards = rewards;
    }

    @Override
    public KillRewardMatchModule createMatchModule(Match match) {
        return new KillRewardMatchModule(match, this.rewards);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static KillRewardModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        List<KillReward> rewards = new ArrayList<>();
        final ItemParser itemParser = context.needModule(ItemParser.class);
        final Optional<ItemModifyModule> itemModifier = context.module(ItemModifyModule.class);

        // Must allow top-level children for legacy support
        for(Element elKillReward : XMLUtils.flattenElements(doc.getRootElement(), ImmutableSet.of("kill-rewards"), ImmutableSet.of("kill-reward", "killreward"), 0)) {
            ImmutableList.Builder<ItemStack> items = ImmutableList.builder();
            for(Element itemEl : elKillReward.getChildren("item")) {
                final ItemStack item = itemParser.parseItem(itemEl, false);
                itemModifier.ifPresent(imm -> imm.applyRules(item));
                items.add(item.immutableCopy());
            }
            Filter filter = context.needModule(FilterParser.class).property(elKillReward, "filter").optional(StaticFilter.ALLOW);
            Kit kit = context.needModule(KitParser.class).property(elKillReward, "kit").optional(KitNode.EMPTY);

            rewards.add(new KillReward(items.build(), filter, kit));
        }

        return new KillRewardModule(rewards);
    }
}
