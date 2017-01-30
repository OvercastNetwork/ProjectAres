package tc.oc.pgm.victory;

import java.util.Optional;
import javax.inject.Inject;

import com.sk89q.minecraft.util.commands.CommandException;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.goals.GoalsMatchResult;
import tc.oc.pgm.map.inject.MapScoped;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.teams.TeamMatchModule;
import tc.oc.pgm.teams.TeamResult;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowSupplier;

@MapScoped
public class VictoryResultParser {

    private final FeatureParser<TeamFactory> teamParser;

    @Inject private VictoryResultParser(FeatureParser<TeamFactory> teamParser) {
        this.teamParser = teamParser;
    }

    private static Optional<MatchResult> generic(String token) {
        switch(token) {
            case "default": return of(new DefaultResult());
            case "tie": return of(new TieResult());
            case "objectives": return of(new GoalsMatchResult());
        }
        return empty();
    }

    public MatchResult parse(Node node) throws InvalidXMLException {
        return generic(node.getValue()).orElseGet(rethrowSupplier(
            () -> new TeamResult(teamParser.parseReference(node))
        ));
    }

    public static MatchResult parse(Match match, String text) throws CommandException {
        return generic(text).orElseGet(rethrowSupplier(
            () -> new CompetitorResult(
                match.module(TeamMatchModule.class)
                     .orElseThrow(() -> new CommandException("No teams in this match"))
                     .fuzzyMatch(text)
                     .orElseThrow(() -> new CommandException("Invalid result type or team name '" + text + "'"))
            )
        ));
    }
}
