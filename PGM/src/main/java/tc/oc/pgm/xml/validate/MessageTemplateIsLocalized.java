package tc.oc.pgm.xml.validate;

import javax.annotation.Nullable;
import javax.inject.Inject;

import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MessageTemplateIsLocalized implements Validation<MessageTemplate> {

    private final MapInfo mapInfo;

    @Inject public MessageTemplateIsLocalized(MapInfo mapInfo) {
        this.mapInfo = mapInfo;
    }

    @Override
    public void validate(MessageTemplate value, @Nullable Node node) throws InvalidXMLException {
        if(mapInfo.phase() == MapDoc.Phase.PRODUCTION && !value.isLocalized()) {
            throw new InvalidXMLException("Message templates in production maps must be localized", node);
        }
    }
}
