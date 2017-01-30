package tc.oc.pgm.xml.parser;

import java.text.MessageFormat;
import javax.inject.Inject;

import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.bukkit.localization.Translator;
import tc.oc.commons.bukkit.util.BukkitUtils;
import tc.oc.commons.core.localization.Formats;
import tc.oc.commons.core.localization.Locales;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MessageTemplateParser extends PrimitiveParser<MessageTemplate> {

    private final Translator translator;
    private final MessageTemplate.Factory factory;

    @Inject MessageTemplateParser(Translator translator, MessageTemplate.Factory factory) {
        this.translator = translator;
        this.factory = factory;
    }

    @Override
    public String readableTypeName() {
        return "message template";
    }

    @Override
    public MessageTemplate parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        if(translator.hasKey(text)) {
            return factory.fromKey(text);
        } else {
            final MessageFormat message;
            try {
                message = Formats.quotedMessage(BukkitUtils.decolorize(text), Locales.DEFAULT_LOCALE);
            } catch(IllegalArgumentException e) {
                throw new FormatException(e.getMessage());
            }
            return factory.literal(message);
        }
    }
}
