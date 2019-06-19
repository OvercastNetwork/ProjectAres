package tc.oc.pgm.modules;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.xml.InvalidXMLException;

import java.util.logging.Logger;

@ModuleDescription(name="Flying Boats")
public class FlyingBoatModule implements MapModule, MatchModuleFactory<FlyingBoatMatchModule> {
    @Override
    public FlyingBoatMatchModule createMatchModule(Match match) {
        return new FlyingBoatMatchModule(match);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static FlyingBoatModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        Element flyingboats = doc.getRootElement().getChild("flyingboats");
        if(flyingboats != null) {
            return new FlyingBoatModule();
        } else {
            return null;
        }
    }
}
