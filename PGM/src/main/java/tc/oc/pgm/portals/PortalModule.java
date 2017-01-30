package tc.oc.pgm.portals;

import java.util.Optional;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.util.Vector;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.eventrules.EventRule;
import tc.oc.pgm.eventrules.EventRuleModule;
import tc.oc.pgm.eventrules.EventRuleScope;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.filters.query.IPlayerQuery;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.regions.RandomPointsValidation;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.regions.TranslatedRegion;
import tc.oc.pgm.regions.Union;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name="Portal", depends={ EventRuleModule.class })
public class PortalModule implements MapModule {
    private static final BaseComponent PROTECT_MESSAGE = new TranslatableComponent("match.portal.protectMessage");

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static PortalModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        final FilterParser filterParser = context.needModule(FilterParser.class);
        final RegionParser regionParser = context.needModule(RegionParser.class);

        for(Element portalEl : XMLUtils.flattenElements(doc.getRootElement(), "portals", "portal")) {
            // Piecewise transform
            PortalTransform transform = PortalTransform.piecewise(parseDoubleTransform(portalEl, "x", DoubleTransform.IDENTITY),
                                                                  parseDoubleTransform(portalEl, "y", DoubleTransform.IDENTITY),
                                                                  parseDoubleTransform(portalEl, "z", DoubleTransform.IDENTITY),
                                                                  parseDoubleTransform(portalEl, "yaw", DoubleTransform.IDENTITY),
                                                                  parseDoubleTransform(portalEl, "pitch", DoubleTransform.IDENTITY));

            // Optional entrance region (required for old proto)
            final Optional<Region> entrance = regionParser.property(portalEl)
                                                          .legacy()
                                                          .optionalUnion();

            // Optional exit region
            Optional<Region> exit = regionParser.property(portalEl, "destination")
                                                .validate(RandomPointsValidation.INSTANCE)
                                                .optional();
            if(exit.isPresent()) {
                // If there is an explicit exit region, create a transform for it and combine
                // it with the piecewise transform (so angle transforms are still applied).
                transform = PortalTransform.concatenate(transform, PortalTransform.regional(entrance, exit.get()));
            } else if(entrance.isPresent() && transform.invertible()) {
                // If no exit region is specified, but there is an entrance region, and the
                // piecewise transform is invertible, infer the exit region from the entrance region.
                exit = Optional.of(new PortalExitRegion(entrance.get(), transform));
            }

            // Dynamic filters
            final Optional<Filter> forward = filterParser.property(portalEl, "forward").respondsTo(IPlayerQuery.class).dynamic().optional();
            final Optional<Filter> reverse = filterParser.property(portalEl, "reverse").respondsTo(IPlayerQuery.class).dynamic().optional();
            final Optional<Filter> transit = filterParser.property(portalEl, "transit").respondsTo(IPlayerQuery.class).dynamic().optional();

            // Check for conflicting dynamic filters
            if(transit.isPresent() && (forward.isPresent() || reverse.isPresent())) {
                throw new InvalidXMLException("Cannot combine 'transit' property with 'forward' or 'transit' properties", portalEl);
            }

            // Check for conflicting region and dynamic filter at each end of the portal
            if(entrance.isPresent() && (forward.isPresent() || transit.isPresent())) {
                throw new InvalidXMLException("Cannot combine an entrance region with 'forward' or 'transit' properties", portalEl);
            }

            if(exit.isPresent() && (reverse.isPresent() || transit.isPresent())) {
                throw new InvalidXMLException("Cannot combine an exit region with 'reverse' or 'transit' properties", portalEl);
            }

            // Figure out the forward trigger, from the dynamic filters or entrance region
            final Filter forwardFinal = Optionals.first(forward, transit, entrance)
                                                 .orElseThrow(() -> new InvalidXMLException("Portal must have an entrance region, or one of 'forward' or 'transit' properties", portalEl));

            // Figure out the (optional) reverse trigger, from dynamic filters or exit region
            final Optional<Filter> reverseFinal = Optionals.first(reverse, transit.map(InverseFilter::new), exit);

            // Portal is always bidirectional if a reverse dynamic filter is specified,
            // otherwise it must be enabled explicitly.
            final boolean bidirectional = reverse.isPresent() || transit.isPresent() || XMLUtils.parseBoolean(portalEl, "bidirectional").optional(false);
            if(bidirectional && !transform.invertible()) {
                throw new InvalidXMLException("Bidirectional portal must have an invertible transform", portalEl);
            }

            // Passive filters
            final Filter participantFilter = filterParser.property(portalEl, "filter")
                                                         .optional(StaticFilter.ALLOW);
            final Filter observerFilter = filterParser.property(portalEl, "observers")
                                                      .optional(StaticFilter.ALLOW);

            boolean sound = XMLUtils.parseBoolean(portalEl.getAttribute("sound"), true);
            boolean smooth = XMLUtils.parseBoolean(portalEl.getAttribute("smooth"), false);

            // Protect the entrance/exit
            final Attribute attrProtect = portalEl.getAttribute("protect");
            if(XMLUtils.parseBoolean(attrProtect, false)) {
                entrance.orElseThrow(() -> new InvalidXMLException("Cannot protect a portal without an entrance region", attrProtect));
                protectRegion(context, entrance.get());
                exit.ifPresent(r -> protectRegion(context, r));
            }

            context.features().define(portalEl, new PortalImpl(forwardFinal, transform, participantFilter, observerFilter, sound, smooth));
            if(bidirectional) {
                context.features().define(portalEl, new PortalImpl(reverseFinal.get(), transform.inverse(), participantFilter, observerFilter, sound, smooth));
            }
        }

        return context.features().containsAny(Portal.class) ? new PortalModule()
                                                            : null;
    }

    /**
     * Use an {@link EventRule} to protect the given entrance/exit {@link Region}.
     *
     * The region is extended up by 2m to allow for the height of the player.
     */
    private static void protectRegion(MapModuleContext context, Region region) {
        region = Union.of(region,
                          TranslatedRegion.translate(region, new Vector(0, 1, 0)),
                          TranslatedRegion.translate(region, new Vector(0, 2, 0)));

        context.needModule(EventRuleModule.class)
               .eventRuleContext()
               .prepend(EventRule.newEventFilter(EventRuleScope.BLOCK_PLACE,
                                                 region,
                                                 StaticFilter.DENY,
                                                 PROTECT_MESSAGE,
                                                 false));
    }

    private static DoubleTransform parseDoubleTransform(Element el, String attributeName, DoubleTransform def) throws InvalidXMLException {
        Attribute attr = el.getAttribute(attributeName);
        if(attr == null) {
            return def;
        }
        String text = attr.getValue();
        try {
            if(text.startsWith("@")) {
                double value = Double.parseDouble(text.substring(1));
                return new DoubleTransform.Constant(value);
            } else {
                double value = Double.parseDouble(text);
                return new DoubleTransform.Translate(value);
            }
        }
        catch(NumberFormatException e) {
            throw new InvalidXMLException("Invalid portal coordinate", attr, e);
        }
    }
}
