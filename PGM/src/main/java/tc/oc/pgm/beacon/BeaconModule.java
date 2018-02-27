package tc.oc.pgm.beacon;

import org.bukkit.DyeColor;
import org.bukkit.util.Vector;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

import java.util.logging.Logger;

public abstract class BeaconModule implements MapModule {

    public static class Factory extends MapModuleFactory<BeaconModule> {

        @Override
        public BeaconModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            FilterParser filterParser = context.needModule(FilterParser.class);

            for(Element el : XMLUtils.flattenElements(doc.getRootElement(), "beacons", "beacon")) {
                DyeColor color = XMLUtils.parseDyeColor(el.getAttribute("color"), DyeColor.WHITE);
                Vector location = XMLUtils.parseVector(el.getAttribute("location"));
                if(location == null) {
                    throw new InvalidXMLException("location cannot be null!");
                }
                Integer particleCount = XMLUtils.parseNumber(el.getAttribute("particle-count"), Integer.class, 40);
                Filter visible = filterParser.property(el, "visibility-filter").optional(StaticFilter.ALLOW);
                context.features().define(el, new BeaconDefinitionImpl(visible, particleCount, location, color));
            }

            return null;
        }
    }

}
