package tc.oc.pgm.scoreboard;

import java.util.logging.Logger;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import tc.oc.pgm.blitz.BlitzModule;
import tc.oc.pgm.map.MapInfo;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.modules.InfoModule;
import tc.oc.pgm.score.ScoreModule;
import tc.oc.pgm.teams.TeamModule;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "sidebar", follows = { TeamModule.class, ScoreModule.class, BlitzModule.class })
public class SidebarModule implements MapModule, MatchModuleFactory<SidebarMatchModule> {

    private BaseComponent title = new TranslatableComponent("match.scoreboard.default.title");

    @Override
    public SidebarMatchModule createMatchModule(Match match) {
        return new SidebarMatchModule(match, title);
    }

    public static SidebarModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        return new SidebarModule();
    }

    @Override
    public void postParse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        final MapInfo info = context.needModule(InfoModule.class).getMapInfo();
        if(info.game != null) {
            title = info.game;
        } else {
            for(MapModule module : context.loadedModules()) {
                final BaseComponent name = module.getGameName(context);
                if(name != null) {
                    title = name;
                }
            }
        }
    }
}
