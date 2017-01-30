package tc.oc.pgm.renewable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.matcher.block.MaterialFilter;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.regions.EverywhereRegion;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "Renewable Resources")
public class RenewableModule implements MapModule, MatchModuleFactory<RenewableMatchModule> {
    private static final double DEFAULT_AVOID_PLAYERS_RANGE = 2d;

    private final List<RenewableDefinition> renewableDefinitions = new ArrayList<>();

    @Override
    public RenewableMatchModule createMatchModule(Match match) {
        return new RenewableMatchModule(match, this.renewableDefinitions);
    }

    private static Filter parseFilter(FilterParser parser, Element el, String name, Filter def) throws InvalidXMLException {
        Filter property = parser.parseOptionalProperty(el, name + "-filter").orElse(null);
        List<Filter> inline = new ArrayList<>();
        for(Element child : el.getChildren(name)) {
            inline.add(new MaterialFilter(XMLUtils.parseMaterialPattern(child)));
        }
        if(property == null) {
            if(inline.isEmpty()) {
                return def;
            } else {
                return new AnyFilter(inline);
            }
        } else {
            if(inline.isEmpty()) {
                return property;
            } else {
                return AllFilter.of(property, new AnyFilter(inline));
            }
        }
    }

    public static RenewableModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        RenewableModule renewableModule = new RenewableModule();
        RegionParser regionParser = context.needModule(RegionParser.class);
        FilterParser filterParser = context.needModule(FilterParser.class);

        for(Element elRenewable : XMLUtils.flattenElements(doc.getRootElement(), "renewables", "renewable")) {
            RenewableDefinition renewableDefinition = new RenewableDefinition();
            renewableDefinition.region = regionParser.property(elRenewable, "region").optionalUnion(EverywhereRegion.INSTANCE);

            renewableDefinition.renewableBlocks = parseFilter(filterParser, elRenewable, "renew", StaticFilter.ALLOW);
            renewableDefinition.replaceableBlocks = parseFilter(filterParser, elRenewable, "replace", StaticFilter.ALLOW);
            renewableDefinition.shuffleableBlocks = parseFilter(filterParser, elRenewable, "shuffle", StaticFilter.DENY);

            Attribute attrRate = elRenewable.getAttribute("rate");
            Attribute attrInterval = elRenewable.getAttribute("interval");
            if(attrRate != null) {
                if(attrInterval != null) {
                    throw new InvalidXMLException("Attributes 'rate' and 'interval' cannot be combined", elRenewable);
                } else {
                    renewableDefinition.renewalsPerSecond = XMLUtils.parseNumber(attrRate, Float.class);
                }
            } else {
                if(attrInterval != null) {
                    Duration interval = XMLUtils.parseDuration(attrInterval);
                    renewableDefinition.renewalsPerSecond = 1000f / interval.toMillis();
                    renewableDefinition.rateScaled = true;
                } else {
                    renewableDefinition.renewalsPerSecond = 1f;
                }
            }

            renewableDefinition.growAdjacent = XMLUtils.parseBoolean(
                elRenewable.getAttribute("grow"), true);

            renewableDefinition.particles = XMLUtils.parseBoolean(
                elRenewable.getAttribute("particles"), true);

            renewableDefinition.sound = XMLUtils.parseBoolean(
                elRenewable.getAttribute("sound"), true);

            if(!XMLUtils.parseBoolean(elRenewable.getAttribute("avoid-entities"), true)) {
                // Legacy compatibility
                renewableDefinition.avoidPlayersRange = 0;
            } else {
                renewableDefinition.avoidPlayersRange = XMLUtils.parseNumber(elRenewable.getAttribute("avoid-players"), Double.class, DEFAULT_AVOID_PLAYERS_RANGE);
            }

            renewableModule.renewableDefinitions.add(renewableDefinition);
        }

        return renewableModule;
    }
}
