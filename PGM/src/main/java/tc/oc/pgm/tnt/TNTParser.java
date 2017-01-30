package tc.oc.pgm.tnt;

import javax.inject.Inject;

import org.jdom2.Element;
import java.time.Duration;
import tc.oc.pgm.eventrules.EventRule;
import tc.oc.pgm.eventrules.EventRuleModule;
import tc.oc.pgm.eventrules.EventRuleScope;
import tc.oc.pgm.filters.matcher.CauseFilter;
import tc.oc.pgm.filters.operator.FallthroughFilter;
import tc.oc.pgm.regions.EverywhereRegion;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ElementParser;

public class TNTParser implements ElementParser<TNTProperties> {

    private static final int DEFAULT_DISPENSER_NUKE_LIMIT = 16;
    private static final float DEFAULT_DISPENSER_NUKE_MULTIPLIER = 0.25f;

    private final CauseFilter.Factory causeFilters;
    private final EventRuleModule eventRules;

    @Inject private TNTParser(CauseFilter.Factory causeFilters, EventRuleModule eventRules) {
        this.causeFilters = causeFilters;
        this.eventRules = eventRules;
    }

    @Override
    public TNTProperties parseElement(Element element) throws InvalidXMLException {
        Float yield = null;
        Float power = null;
        boolean instantIgnite = false;
        boolean blockDamage = true;
        Duration fuse = null;
        int dispenserNukeLimit = DEFAULT_DISPENSER_NUKE_LIMIT;
        float dispenserNukeMultiplier = DEFAULT_DISPENSER_NUKE_MULTIPLIER;
        boolean licensing = true;
        boolean friendlyDefuse = true;

        for(Element tntElement : element.getChildren("tnt")) {
            instantIgnite = XMLUtils.parseBoolean(XMLUtils.getUniqueChild(tntElement, "instantignite"), instantIgnite);
            blockDamage = XMLUtils.parseBoolean(XMLUtils.getUniqueChild(tntElement, "blockdamage"), blockDamage);
            yield = XMLUtils.parseNumber(XMLUtils.getUniqueChild(tntElement, "yield"), Float.class, yield);
            power = XMLUtils.parseNumber(XMLUtils.getUniqueChild(tntElement, "power"), Float.class, power);
            dispenserNukeLimit = XMLUtils.parseNumber(XMLUtils.getUniqueChild(tntElement, "dispenser-tnt-limit"), Integer.class, dispenserNukeLimit);
            dispenserNukeMultiplier = XMLUtils.parseNumber(XMLUtils.getUniqueChild(tntElement, "dispenser-tnt-multiplier"), Float.class, dispenserNukeMultiplier);
            licensing = XMLUtils.parseBoolean(XMLUtils.getUniqueChild(tntElement, "licensing"), licensing);
            friendlyDefuse = XMLUtils.parseBoolean(XMLUtils.getUniqueChild(tntElement, "friendly-defuse"), friendlyDefuse);
            fuse = XMLUtils.parseDuration(XMLUtils.getUniqueChild(tntElement, "fuse"), fuse);
        }

        if(!blockDamage) {
            eventRules.eventRuleContext().prepend(EventRule.newEventFilter(
                EventRuleScope.BLOCK_BREAK,
                EverywhereRegion.INSTANCE,
                FallthroughFilter.deny(causeFilters.create(CauseFilter.Cause.EXPLOSION)),
                null,
                false
            ));
        }

        return new TNTProperties(yield, power, instantIgnite, blockDamage, fuse, dispenserNukeLimit, dispenserNukeMultiplier, licensing, friendlyDefuse);
    }
}
