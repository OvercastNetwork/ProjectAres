package tc.oc.pgm.eventrules;

import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowConsumer;

@ModuleDescription(name="Event Rules")
public class EventRuleModule implements MapModule, MatchModuleFactory<EventRuleMatchModule> {
    protected final EventRuleContext eventRuleContext;

    public EventRuleModule(EventRuleContext eventRuleContext) {
        this.eventRuleContext = eventRuleContext;
    }

    public EventRuleContext eventRuleContext() {
        return eventRuleContext;
    }

    @Override
    public EventRuleMatchModule createMatchModule(Match match) {
        return new EventRuleMatchModule(match, this.eventRuleContext);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static EventRuleModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        final EventRuleContext ruleContext = new EventRuleContext();
        final EventRuleParser ruleParser = new EventRuleParser(context, ruleContext);

        for(Element regionRootElement : XMLUtils.getChildren(doc.getRootElement(), "filters", "regions")) {
            for(Element applyEl : regionRootElement.getChildren("apply")) {
                ruleParser.parse(applyEl);
            }
        }

        return new EventRuleModule(ruleContext);
    }

    @Override
    public void postParse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        context.features().all(EventRule.class).forEach(rethrowConsumer(rule -> {
            if(rule.lendKit() && !rule.kit().isRemovable()) {
                throw new InvalidXMLException("Specified lend-kit is not removable", context.features().definitionNode(rule));
            }
        }));
    }
}
