package tc.oc.pgm.hunger;

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

@ModuleDescription(name="Hunger")
public class HungerModule implements MapModule, MatchModuleFactory<HungerMatchModule> {
    @Override
    public HungerMatchModule createMatchModule(Match match) {
        return new HungerMatchModule(match);
    }

    public static HungerModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        boolean depletion = true;

        for(Element elHunger : doc.getRootElement().getChildren("hunger")) {
            depletion = XMLUtils.parseBoolean(elHunger, "depletion")
                                .optional(depletion);
        }

        if(!depletion) {
            return new HungerModule();
        } else {
            return null;
        }
    }
}
