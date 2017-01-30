package tc.oc.pgm.broadcast;

import java.time.Duration;
import javax.inject.Inject;

import com.google.common.collect.Range;
import org.jdom2.Element;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.Parser;
import tc.oc.pgm.xml.parser.PrimitiveParser;
import tc.oc.pgm.xml.property.DurationProperty;
import tc.oc.pgm.xml.property.NumberProperty;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

public class BroadcastParser implements FeatureDefinitionParser<Broadcast> {

    private final PropertyBuilderFactory<Integer, NumberProperty<Integer>> integers;
    private final PropertyBuilderFactory<Duration, DurationProperty> durations;
    private final PrimitiveParser<Broadcast.Type> broadcastTypeParser;
    private final FilterParser filterParser;
    private final Parser<MessageTemplate> messageParser;

    @Inject BroadcastParser(FilterParser filterParser,
                            Parser<MessageTemplate> messageParser,
                            PropertyBuilderFactory<Integer, NumberProperty<Integer>> integers,
                            PropertyBuilderFactory<Duration, DurationProperty> durations,
                            PrimitiveParser<Broadcast.Type> broadcastTypeParser) {

        this.filterParser = filterParser;
        this.messageParser = messageParser;
        this.integers = integers;
        this.durations = durations;
        this.broadcastTypeParser = broadcastTypeParser;
    }

    @Override
    public Broadcast parseElement(Element el) throws InvalidXMLException {
        final Node node = new Node(el);

        final Duration after = durations.property(el, "after").required();

        Duration every = durations.property(el, "every")
                                  .optional(null);

        int count = integers.property(el, "count")
                            .range(Range.atLeast(1))
                            .infinity(true)
                            .optional(1);

        if(count > 1 && every == null) {
            // If a repeat count is specified but no interval, use the initial delay as the interval
            every = after;
        } else if(count == 1 && every != null) {
            // If a repeat interval is specified but no count, repeat forever
            count = Integer.MAX_VALUE;
        }

        if(every != null && Comparables.lessThan(every, Broadcast.MIN_INTERVAL)) {
            throw new InvalidXMLException(
                "Broadcast repeat interval must be at least " +
                Broadcast.MIN_INTERVAL.toMillis() + "ms",
                el
            );
        }

        return new Broadcast(
            broadcastTypeParser.parse(node, el.getName()),
            after, every, count,
            messageParser.parse(node),
            filterParser.property(el, "filter").optional(StaticFilter.ALLOW)
        );
    }
}
