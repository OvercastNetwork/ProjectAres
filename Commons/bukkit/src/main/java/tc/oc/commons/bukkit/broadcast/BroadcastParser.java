package tc.oc.commons.bukkit.broadcast;

import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;

import java.time.Duration;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tc.oc.commons.bukkit.broadcast.model.BroadcastPrefix;
import tc.oc.commons.bukkit.broadcast.model.BroadcastSchedule;
import tc.oc.commons.bukkit.broadcast.model.BroadcastSet;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.minecraft.server.ServerFilter;
import tc.oc.parse.ParseException;
import tc.oc.parse.validate.NonZeroDuration;
import tc.oc.parse.validate.NormalizedPath;
import tc.oc.parse.xml.DocumentParser;
import tc.oc.parse.xml.ElementParser;
import tc.oc.parse.xml.NodeParser;
import tc.oc.parse.xml.ValidatingNodeParser;
import tc.oc.parse.xml.XML;

public class BroadcastParser implements DocumentParser<List<BroadcastSchedule>> {

    private final NodeParser<Duration> durationParser;
    private final NodeParser<Path> pathParser;
    private final NodeParser<BroadcastPrefix> prefixParser;
    private final ElementParser<ServerFilter> serverFilterParser;

    @Inject BroadcastParser(NodeParser<Duration> durationParser, NodeParser<Path> pathParser, NodeParser<BroadcastPrefix> prefixParser, ElementParser<ServerFilter> serverFilterParser) {
        this.durationParser = new ValidatingNodeParser<>(durationParser, new NonZeroDuration());
        this.pathParser = new ValidatingNodeParser<>(pathParser, new NormalizedPath());
        this.prefixParser = prefixParser;
        this.serverFilterParser = serverFilterParser;
    }

    @Override
    public List<BroadcastSchedule> parse(Document document) throws ParseException {
        return XML.childrenNamed(document.getDocumentElement(), "broadcasts")
                  .flatMap(el -> XML.childrenNamed(el, "schedule"))
                  .map(this::parseSchedule)
                  .collect(Collectors.toImmutableList());
    }

    public BroadcastSchedule parseSchedule(Element el) throws ParseException {
        Duration delay = Duration.ZERO;
        Attr delayAttr = el.getAttributeNode("delay");
        if (delayAttr != null) {
            delay = durationParser.parse(delayAttr);
        }
        return new BroadcastSchedule(
            delay, durationParser.parse(XML.requireAttr(el, "interval")),
            serverFilterParser.parse(el), XML.childrenNamed(el, "messages").map(this::parseMessages)
        );
    }

    public BroadcastSet parseMessages(Element el) {
        return new BroadcastSet(
            pathParser.parse(XML.requireAttr(el, "path")),
            prefixParser.parse(XML.requireAttr(el, "prefix"))
        );
    }
}
