package tc.oc.pgm.teams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Range;
import org.jdom2.Document;
import org.jdom2.Element;
import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.map.MapModule;
import tc.oc.pgm.map.MapModuleContext;
import tc.oc.pgm.map.MapModuleFactory;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModuleFactory;
import tc.oc.pgm.module.ModuleDescription;
import tc.oc.pgm.modules.InfoModule;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

@ModuleDescription(name = "Team", requires = { InfoModule.class })
public class TeamModule implements MapModule, MatchModuleFactory<TeamMatchModule> {

    private final List<TeamFactory> teams;
    private final Optional<Boolean> requireEven;

    public TeamModule(List<TeamFactory> teams, Optional<Boolean> requireEven) {
        this.teams = teams;
        this.requireEven = requireEven;
    }

    @Override
    public TeamMatchModule createMatchModule(Match match) {
        return new TeamMatchModule(match, requireEven);
    }

    @Override
    public Range<Integer> getPlayerLimits() {
        return Range.closed(teams.stream().mapToInt(team -> team.getMinPlayers().orElse(0)).sum(),
                            teams.stream().mapToInt(TeamFactory::getMaxPlayers).sum());
    }


    // ---------------------
    // ---- XML Parsing ----
    // ---------------------

    public static class Factory extends MapModuleFactory<TeamModule> {
        @Inject Provider<List<TeamFactory>> teamsProvider;

        @Override
        public @Nullable TeamModule parse(MapModuleContext context, Logger logger, Document doc) throws InvalidXMLException {
            final List<TeamFactory> teams = teamsProvider.get();
            if(teams.isEmpty()) return null;

            final Map<String, TeamFactory> byName = new HashMap<>();
            for(TeamFactory team : teams) {
                final String name = team.getDefaultName();
                final TeamFactory dupe = byName.put(name, team);
                if(dupe != null) {
                    String msg = "Duplicate team name '" + name + "'";
                    final Element dupeNode = context.features().definitionNode(dupe);
                    if(dupeNode != null) {
                        msg += " (other team defined by " + Node.of(dupeNode).describeWithLocation() + ")";
                    }
                    throw new InvalidXMLException(msg, context.features().definitionNode(team));
                }
            }

            Optional<Boolean> requireEven = Optional.empty();
            for(Element elTeam : XMLUtils.flattenElements(doc.getRootElement(), "teams", "team")) {
                requireEven = Optionals.first(XMLUtils.parseBoolean(elTeam, "even").optional(), requireEven);
            }

            return new TeamModule(teams, requireEven);
        }
    }
}
