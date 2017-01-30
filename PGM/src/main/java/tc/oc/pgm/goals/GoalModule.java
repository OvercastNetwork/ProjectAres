package tc.oc.pgm.goals;

import java.util.logging.Logger;
import javax.annotation.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jdom2.Document;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name = "goals")
public class GoalModule implements MapModule {

    private static final BaseComponent GAME = new TranslatableComponent("match.scoreboard.objectives.title");

    @Override
    public @Nullable BaseComponent getGameName(MapModuleContext context) {
        return context.features().containsAny(GoalDefinition.class) ? GAME : null;
    }

    public static class Factory extends MapModuleFactory<GoalModule> {
        @Override
        public GoalModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            return new GoalModule();
        }
    }
}
