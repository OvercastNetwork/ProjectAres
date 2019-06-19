package tc.oc.pgm.rage;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.blitz.BlitzModule;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;

@ModuleDescription(name = "Rage", follows = { BlitzModule.class })
public class RageModule implements MapModule, MatchModuleFactory<RageMatchModule> {

    private final boolean blitz;

    public RageModule(boolean blitz) {
        this.blitz = blitz;
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return Collections.singleton(MapDoc.Gamemode.rage);
    }

    private static final BaseComponent GAME = new TranslatableComponent("match.scoreboard.rage.title");
    @Override
    public BaseComponent getGameName(MapModuleContext context) {
        return blitz ? GAME : null;
    }

    @Override
    public RageMatchModule createMatchModule(Match match) {
        return new RageMatchModule(match);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static RageModule parse(MapModuleContext context, Logger logger, Document doc) {

        if(doc.getRootElement().getChild("rage") != null) {
            return new RageModule(context.module(BlitzModule.class).filter(BlitzModule::active).isPresent());
        } else {
            return null;
        }
    }
}
