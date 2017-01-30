package tc.oc.pgm.pickup;

import java.util.logging.Logger;

import org.bukkit.entity.EntityType;
import org.jdom2.Document;
import org.jdom2.Element;
import java.time.Duration;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.kits.Kit;
import tc.oc.pgm.kits.KitNode;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.regions.RandomPointsValidation;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "Pickup")
public abstract class PickupModule implements MapModule {
    /**
     * HACK: This module is never instantiated, we just need it to register the parser
     */
    private PickupModule() { throw new IllegalStateException(); }

    public static class Factory extends MapModuleFactory<PickupModule> {
        @Override
        public PickupModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            KitParser kitParser = context.needModule(KitParser.class);
            FilterParser filterParser = context.needModule(FilterParser.class);
            RegionParser regionParser = context.needModule(RegionParser.class);

            for(Element el : XMLUtils.flattenElements(doc.getRootElement(), "pickups", "pickup")) {
                String name = el.getAttributeValue("name");
                EntityType appearance = XMLUtils.parseEnum(Node.fromAttr(el, "appearance"), EntityType.class, "entity type", EntityType.ENDER_CRYSTAL);
                if(appearance != EntityType.ENDER_CRYSTAL) {
                    throw new InvalidXMLException("Only ender crystal appearances are supported right now", el);
                }
                Filter visible = filterParser.property(el, "spawn-filter").optional(StaticFilter.ALLOW);
                Filter pickup = filterParser.property(el, "pickup-filter").optional(StaticFilter.ALLOW);
                Region region = regionParser.property(el, "region").validate(RandomPointsValidation.INSTANCE).required();
                Kit kit = kitParser.property(el, "kit").optional(KitNode.EMPTY);
                Duration refresh = XMLUtils.parseDuration(Node.fromAttr(el, "respawn-time"), Duration.ofSeconds(3));
                Duration cooldown = XMLUtils.parseDuration(Node.fromAttr(el, "pickup-time"), Duration.ofSeconds(3));
                boolean effects = XMLUtils.parseBoolean(Node.fromAttr(el, "effects"), true);
                boolean sounds = XMLUtils.parseBoolean(Node.fromAttr(el, "sounds"), true);
                context.features().define(el, new PickupDefinitionImpl(name, appearance, visible, pickup, region, kit, refresh, cooldown, effects, sounds));
            }

            return null;
        }
    }

}
