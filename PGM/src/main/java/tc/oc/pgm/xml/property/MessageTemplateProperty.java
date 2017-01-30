package tc.oc.pgm.xml.property;

import java.text.Format;
import javax.inject.Inject;

import com.google.common.collect.Range;
import com.google.inject.assistedinject.Assisted;
import org.jdom2.Element;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.util.Ranges;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.MessageTemplateParser;

public class MessageTemplateProperty extends PropertyBuilder<MessageTemplate, MessageTemplateProperty> {

    @Inject private MessageTemplateProperty(@Assisted Element parent, @Assisted String name, MessageTemplateParser parser, MapModuleContext context) {
        super(parent, name, parser);
    }

    public MessageTemplateProperty placeholders(int count) {
        return placeholders(Range.singleton(count));
    }

    public MessageTemplateProperty placeholders(Range<Integer> range) {
        validate((template, node) -> {
            final Format[] args = template.format().getFormatsByArgumentIndex();
            if(!range.contains(args.length)) {
                throw new InvalidXMLException("Template should contain " + Ranges.describe(range) +
                                              " placeholders, but it actually contains " + args.length,
                                              node);
            }
        });
        return this;
    }
}
