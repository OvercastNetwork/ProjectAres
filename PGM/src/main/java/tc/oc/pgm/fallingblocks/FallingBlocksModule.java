package tc.oc.pgm.fallingblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name="Falling Blocks")
public class FallingBlocksModule implements MapModule, MatchModuleFactory<FallingBlocksMatchModule> {
    private final List<FallingBlocksRule> rules;

    public FallingBlocksModule(List<FallingBlocksRule> rules) {
        this.rules = rules;
    }

    @Override
    public FallingBlocksMatchModule createMatchModule(Match match) {
        return new FallingBlocksMatchModule(match, this.rules);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static FallingBlocksModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        List<FallingBlocksRule> rules = new ArrayList<>();
        FilterParser filterParser = context.needModule(FilterParser.class);

        for(Element elRule : XMLUtils.flattenElements(doc.getRootElement(), "falling-blocks", "rule")) {
            Filter fall = filterParser.parseProperty(elRule, "filter");
            Filter stick = filterParser.property(elRule, "sticky").optional(StaticFilter.DENY);
            int delay = XMLUtils.parseNumber(Node.fromChildOrAttr(elRule, "delay"), Integer.class, FallingBlocksRule.DEFAULT_DELAY);

            rules.add(new FallingBlocksRule(fall, stick, delay));
        }
        return rules.isEmpty() ? null : new FallingBlocksModule(rules);
    }
}
