package tc.oc.commons.bukkit.localization;

import java.util.Map;
import javax.inject.Inject;

import net.md_5.bungee.api.chat.BaseComponent;
import org.w3c.dom.Document;
import tc.oc.commons.bukkit.markup.MarkupParser;
import tc.oc.commons.core.stream.Collectors;
import tc.oc.parse.ParseException;
import tc.oc.parse.xml.DocumentParser;
import tc.oc.parse.xml.XML;

public class MessageMapParser implements DocumentParser<Map<String, BaseComponent>> {

    private final MarkupParser markupParser;

    @Inject MessageMapParser(MarkupParser markupParser) {
        this.markupParser = markupParser;
    }

    @Override
    public Map<String, BaseComponent> parse(Document document) throws ParseException {
        return XML.childrenNamed(document.getDocumentElement(), "string")
                  .collect(Collectors.toImmutableMap(el -> XML.requireAttr(el, "name").getValue(),
                                                     markupParser::content));
    }
}
