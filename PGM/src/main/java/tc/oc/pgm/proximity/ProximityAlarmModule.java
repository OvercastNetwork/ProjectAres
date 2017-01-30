package tc.oc.pgm.proximity;

import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Sets;
import org.bukkit.ChatColor;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "Proximity Alarm")
public class ProximityAlarmModule implements MapModule, MatchModuleFactory<ProximityAlarmMatchModule> {
    private final Set<ProximityAlarmDefinition> definitions;

    public ProximityAlarmModule(Set<ProximityAlarmDefinition> definitions) {
        this.definitions = definitions;
    }

    @Override
    public ProximityAlarmMatchModule createMatchModule(Match match) {
        return new ProximityAlarmMatchModule(match, this.definitions);
    }

    public static ProximityAlarmModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        Set<ProximityAlarmDefinition> definitions = Sets.newHashSet();

        for(Element elAlarm : XMLUtils.flattenElements(doc.getRootElement(), "proximity-alarms", "proximity-alarm")) {
            definitions.add(parseDefinition(context, elAlarm));
        }

        if(definitions.isEmpty()) {
            return null;
        } else {
            return new ProximityAlarmModule(definitions);
        }
    }

    public static ProximityAlarmDefinition parseDefinition(MapModuleContext context, Element elAlarm) throws InvalidXMLException {
        ProximityAlarmDefinition definition = new ProximityAlarmDefinition();
        FilterParser filterParser = context.needModule(FilterParser.class);
        definition.detectFilter = filterParser.parseProperty(elAlarm, "detect");
        definition.alertFilter = filterParser.property(elAlarm, "notify").optionalGet(() -> new InverseFilter(definition.detectFilter));
        definition.detectRegion = context.needModule(RegionParser.class).property(elAlarm, "region").required();
        definition.alertMessage = elAlarm.getAttributeValue("message"); // null = no message

        if(definition.alertMessage != null) {
            definition.alertMessage = ChatColor.translateAlternateColorCodes('`', definition.alertMessage);
        }
        Attribute attrFlareRadius = elAlarm.getAttribute("flare-radius");
        definition.flares = attrFlareRadius != null;
        if(definition.flares) {
            definition.flareRadius = XMLUtils.parseNumber(attrFlareRadius, Double.class);
        }

        return definition;
    }
}
