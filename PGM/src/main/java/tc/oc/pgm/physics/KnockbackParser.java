package tc.oc.pgm.physics;

import java.util.Optional;

import org.jdom2.Element;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.parser.ElementParser;

public class KnockbackParser implements ElementParser<Optional<KnockbackSettings>> {
    @Override
    public Optional<KnockbackSettings> parseElement(Element element) throws InvalidXMLException {
        KnockbackSettings settings = KnockbackSettings.DEFAULT;
        boolean changed = false;

        for(Element el : XMLUtils.getChildren(element, "knockback", "knockback-1")) { // HACK: versioning
            changed = true;
            settings = new KnockbackSettings(
                XMLUtils.parseNumber(el.getAttribute("pitch"), Double.class, settings.pitch),
                XMLUtils.parseNumber(el.getAttribute("walk-power"), Double.class, settings.walkPower),
                XMLUtils.parseNumber(el.getAttribute("sprint-power"), Double.class, settings.sprintPower),
                XMLUtils.parseNumber(el.getAttribute("sprint-threshold"), Double.class, settings.sprintThreshold),
                XMLUtils.parseNumber(el.getAttribute("recoil-ground"), Double.class, settings.recoilGround),
                XMLUtils.parseNumber(el.getAttribute("recoil-air"), Double.class, settings.recoilAir)
            );
        }

        return changed ? Optional.of(settings)
                       : Optional.empty();
    }
}
