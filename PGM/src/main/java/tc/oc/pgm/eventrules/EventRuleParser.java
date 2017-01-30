package tc.oc.pgm.eventrules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.util.Vector;
import org.jdom2.Attribute;
import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.operator.ChainFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.kits.KitParser;
import tc.oc.pgm.kits.RemovableValidation;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.regions.EverywhereRegion;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

public class EventRuleParser {
    private final MapModuleContext context;
    private final FilterParser filterParser;
    private final RegionParser regionParser;
    private final KitParser kitParser;
    private final EventRuleContext ruleContext;
    private final SemanticVersion proto;

    public EventRuleParser(MapModuleContext context, EventRuleContext ruleContext) {
        this.context = context;
        this.ruleContext = ruleContext;
        this.filterParser = context.needModule(FilterParser.class);
        this.regionParser = context.needModule(RegionParser.class);
        this.kitParser = context.needModule(KitParser.class);

        this.proto = context.getProto();
    }

    private boolean useId() {
        return proto.isNoOlderThan(ProtoVersions.FILTER_FEATURES);
    }

    private void add(Element el, EventRule rule) throws InvalidXMLException {
        ruleContext.add(context.features().define(el, EventRule.class, rule));
    }

    public void parse(Element el) throws InvalidXMLException {
        final Region region;
        if(useId()) {
            // Multiple regions are unioned, but the default is NOT an empty union
            region = regionParser.property(el).optionalUnion(EverywhereRegion.INSTANCE);
        } else {
            region = regionParser.parseReferenceAndChildUnion(el);
        }

        BaseComponent message = XMLUtils.parseFormattedText(el, "message");

        boolean earlyWarning = XMLUtils.parseBoolean(el.getAttribute("early-warning"), false);
        Filter effectFilter = filterParser.parseOptionalProperty(el, "filter").orElse(null);

        kitParser.property(el, "kit").optional().ifPresent(rethrowConsumer(
            kit -> add(el, EventRule.newKitRegion(EventRuleScope.EFFECT, region, effectFilter, kit, false))
        ));

        kitParser.property(el, "lend-kit").validate(RemovableValidation.get()).optional().ifPresent(rethrowConsumer(
            kit -> add(el, EventRule.newKitRegion(EventRuleScope.EFFECT, region, effectFilter, kit, true))
        ));

        Attribute attrVelocity = el.getAttribute("velocity");
        if(attrVelocity != null) {
            // Legacy support
            String velocityText = attrVelocity.getValue();
            if(velocityText.charAt(0) == '@') velocityText = velocityText.substring(1);
            Vector velocity = XMLUtils.parseVector(attrVelocity, velocityText);
            add(el, EventRule.newVelocityRegion(EventRuleScope.EFFECT, region, effectFilter, velocity));
        }

        for(String tag : EventRuleScope.byTag.keySet()) {
            Filter filter;
            if(useId()) {
                filter = filterParser.parseOptionalProperty(el, tag).orElse(null);
            } else {
                // Legacy syntax allows a list of filter names in the attribute
                Node node = Node.fromAttr(el, tag);
                if(node == null) {
                    filter = null;
                } else {
                    List<Filter> filters = new ArrayList<>();
                    for(String name : Splitter.on(" ").split(node.getValue())) {
                        filters.add(filterParser.parseReference(node, name));
                    }
                    switch(filters.size()) {
                        case 0: filter = null; break;
                        case 1: filter = filters.get(0); break;
                        default: filter = ChainFilter.reverse(filters);
                    }
                }
            }

            if(filter != null) {
                for(EventRuleScope scope : EventRuleScope.byTag.get(tag)) {
                    add(el, EventRule.newEventFilter(scope, region, filter, message, earlyWarning));
                }
            }
        }
    }
}
