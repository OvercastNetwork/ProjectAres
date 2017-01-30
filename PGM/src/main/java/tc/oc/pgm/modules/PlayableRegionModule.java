package tc.oc.pgm.modules;

import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.regions.Region;
import tc.oc.pgm.regions.RegionParser;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name="Playable Region")
public class PlayableRegionModule implements MapModule, MatchModuleFactory<PlayableRegionMatchModule> {
    protected final Region playableRegion;

    public PlayableRegionModule(Region playableRegion) {
        this.playableRegion = playableRegion;
    }

    @Override
    public PlayableRegionMatchModule createMatchModule(Match match) {
        return new PlayableRegionMatchModule(match, this.playableRegion);
    }

    public static PlayableRegionModule parse(MapModuleContext context, Logger log, Document doc) throws InvalidXMLException {
        Element playableRegionElement = doc.getRootElement().getChild("playable");
        if(playableRegionElement != null) {
            return new PlayableRegionModule(context.needModule(RegionParser.class).property(playableRegionElement).legacy().union());
        }
        return null;
    }
}
