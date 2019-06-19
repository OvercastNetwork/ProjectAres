package tc.oc.pgm.blitz;

import tc.oc.commons.core.IterableUtils;
import tc.oc.commons.core.util.MapUtils;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.teams.Team;
import tc.oc.pgm.teams.TeamFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlitzProperties {

    public final Map<TeamFactory, Integer> teams;
    public final Map<Filter, Integer> individuals;
    public final Lives.Type type;
    public final boolean broadcast;

    private final boolean multi;
    private final boolean empty;

    public BlitzProperties(Map<TeamFactory, Integer> teams, Map<Filter, Integer> individuals, Lives.Type type, boolean broadcast) {
        this.teams = teams;
        this.individuals = individuals;
        this.type = type;
        this.broadcast = broadcast;
        this.multi = IterableUtils.any(IterableUtils.concat(teams.values(), individuals.values()), i -> i != 1);
        this.empty = teams.isEmpty() && individuals.isEmpty();
    }

    public static BlitzProperties none() {
        return new BlitzProperties(new HashMap<>(), new HashMap<>(), Lives.Type.INDIVIDUAL, false);
    }

    public static BlitzProperties individuals(Map<Filter, Integer> individuals, boolean broadcast) {
        return new BlitzProperties(new HashMap<>(), individuals, Lives.Type.INDIVIDUAL, broadcast);
    }

    public static BlitzProperties teams(Map<TeamFactory, Integer> teams, boolean broadcast) {
        return new BlitzProperties(teams, new HashMap<>(), Lives.Type.TEAM, broadcast);
    }

    public static BlitzProperties create(Match match, int lives, Lives.Type type) {
        return new BlitzProperties(
            match.competitors().filter(c -> c instanceof Team).map(c -> ((Team) c).getDefinition()).collect(Collectors.toMap(Function.identity(), c -> lives)),
            MapUtils.merge(new HashMap<>(), StaticFilter.ALLOW, lives),
            type,
            true
        );
    }

    public boolean multipleLives() {
        return multi;
    }

    public boolean empty() {
        return empty;
    }

}
