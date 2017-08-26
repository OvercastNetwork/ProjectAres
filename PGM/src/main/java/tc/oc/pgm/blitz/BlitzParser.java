package tc.oc.pgm.blitz;

import com.google.common.collect.Range;
import com.google.inject.Provider;
import org.jdom2.Element;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.parser.FilterParser;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.ElementParser;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static tc.oc.pgm.blitz.BlitzProperties.*;

public class BlitzParser implements ElementParser<BlitzProperties> {

    private final FilterParser filters;
    private final Provider<List<TeamFactory>> factories;

    @Inject private BlitzParser(FilterParser filters, Provider<List<TeamFactory>> factories) {
        this.filters = filters;
        this.factories = factories;
    }

    @Override
    public BlitzProperties parseElement(Element element) throws InvalidXMLException {
        boolean broadcast = true;
        int global = -1;

        Map<Filter, Integer> individuals = new HashMap<>();
        Map<TeamFactory, Integer> teams = factories.get().stream()
                .filter(team -> team.getLives().isPresent())
                .collect(Collectors.toMap(Function.identity(), team -> team.getLives().get()));

        for(Element el : XMLUtils.getChildren(element, "blitz")) {
            broadcast = XMLUtils.parseBoolean(Node.fromChildOrAttr(el, "broadcast", "broadcastLives"), broadcast);
            global = XMLUtils.parseNumber(Node.fromChildOrAttr(el, "lives"), Integer.class, Range.atLeast(1), global);
            if(global != -1) {
                individuals.put(StaticFilter.ALLOW, global);
            } else {
                for(Element e : XMLUtils.getChildren(el, "rule")) {
                    individuals.put(
                        filters.parse(Node.fromChildOrAttr(e, "filter")),
                        XMLUtils.parseNumber(Node.fromChildOrAttr(e, "lives"), Integer.class, Range.atLeast(1), 1)
                    );
                }
            }
        }

        if(!individuals.isEmpty() && teams.isEmpty()) {
            return individuals(individuals, broadcast);
        } else if(individuals.isEmpty() && !teams.isEmpty()) {
            return teams(teams, broadcast);
        } else if(!individuals.isEmpty() && !teams.isEmpty()) {
            throw new InvalidXMLException("Cannot define both team respawns and blitz");
        } else {
            return none();
        }

    }

}
