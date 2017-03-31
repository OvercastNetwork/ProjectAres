package tc.oc.pgm.gamerules;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.mutation.MutationMapModule;
import tc.oc.pgm.xml.InvalidXMLException;

@ModuleDescription(name="Gamerules", follows = MutationMapModule.class)
public class GameRulesModule implements MapModule, MatchModuleFactory<GameRulesMatchModule> {

    private Map<String, String> gameRules;

    private GameRulesModule(Map<String, String> gamerules) {
        this.gameRules = gamerules;
    }

    public GameRulesMatchModule createMatchModule(Match match) {
        return new GameRulesMatchModule(match, this.gameRules);
    }

    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static GameRulesModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
        Map<String, String> gameRules = new HashMap<>();

        for (Element gameRulesElement : doc.getRootElement().getChildren("gamerules")) {
            for (Element gameRuleElement : gameRulesElement.getChildren()) {
                String gameRule = gameRuleElement.getName();
                String value = gameRuleElement.getValue();

                if(gameRule == null) {
                    throw new InvalidXMLException(gameRuleElement.getName() + " is not a valid gamerule", gameRuleElement);
                }
                if(value == null) {
                    throw new InvalidXMLException("Missing value for gamerule " + gameRule, gameRuleElement);
                } else if (!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
                    throw new InvalidXMLException(gameRuleElement.getValue() + " is not a valid gamerule value", gameRuleElement);
                }
                if(gameRules.containsKey(gameRule)){
                    throw new InvalidXMLException(gameRule + " has already been specified", gameRuleElement);
                }

                gameRules.put(gameRule, value);
            }
        }
        return new GameRulesModule(gameRules);
    }

    public ImmutableMap<String, String> getGameRules() {
        return ImmutableMap.copyOf(this.gameRules);
    }

}
