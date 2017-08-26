package tc.oc.pgm.ghostsquadron;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.api.docs.virtual.MapDoc;
import tc.oc.pgm.classes.ClassMatchModule;
import tc.oc.pgm.classes.ClassModule;
import tc.oc.pgm.blitz.BlitzModule;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;

@ModuleDescription(name = "Ghost Squadron", depends = { ClassModule.class }, follows = { BlitzModule.class })
public class GhostSquadronModule implements MapModule, MatchModuleFactory<GhostSquadronMatchModule> {

    private static final BaseComponent GAME = new TranslatableComponent("match.scoreboard.gs.title");
    @Override
    public BaseComponent getGameName(MapModuleContext context) {
        return GAME;
    }

    @Override
    public Set<MapDoc.Gamemode> getGamemodes(MapModuleContext context) {
        return Collections.singleton(MapDoc.Gamemode.gs);
    }

    @Override
    public GhostSquadronMatchModule createMatchModule(Match match) {
        return new GhostSquadronMatchModule(match, match.getMatchModule(ClassMatchModule.class));
    }

    public static GhostSquadronModule parse(MapModuleContext context, Logger logger, Document doc) {
        Element ghostSquadronEl = doc.getRootElement().getChild("ghostsquadron");
        if(ghostSquadronEl == null) {
            return null;
        } else {
            return new GhostSquadronModule();
        }
    }
}
