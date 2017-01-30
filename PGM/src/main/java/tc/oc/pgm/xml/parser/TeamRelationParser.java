package tc.oc.pgm.xml.parser;

import org.bukkit.scoreboard.Team;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class TeamRelationParser extends PrimitiveParser<Team.OptionStatus> {
    @Override
    protected Team.OptionStatus parseInternal(Node node, String text) throws FormatException, InvalidXMLException {
        switch(node.getValue()) {
            case "yes":
            case "on":
            case "true":
                return Team.OptionStatus.ALWAYS;

            case "no":
            case "off":
            case "false":
                return Team.OptionStatus.NEVER;

            case "ally":
            case "allies":
                return Team.OptionStatus.FOR_OWN_TEAM;

            case "enemy":
            case "enemies":
                return Team.OptionStatus.FOR_OTHER_TEAMS;

            default:
                throw new InvalidXMLException("Invalid team relationship", node);
        }
    }
}
