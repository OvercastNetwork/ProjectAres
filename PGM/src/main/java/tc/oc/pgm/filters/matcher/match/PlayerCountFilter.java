package tc.oc.pgm.filters.matcher.match;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Range;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.features.FeatureDefinition;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.FilterListener;
import tc.oc.pgm.filters.FilterMatchModule;
import tc.oc.pgm.filters.matcher.TypedFilter;
import tc.oc.pgm.filters.matcher.player.ParticipatingFilter;
import tc.oc.pgm.filters.query.IMatchQuery;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchPlayer;

public class PlayerCountFilter extends TypedFilter.Impl<IMatchQuery> implements FeatureFactory<PlayerCountFilter.Reactor> {

    private final @Inspect Range<Integer> range;
    private final @Inspect Filter filter;

    public PlayerCountFilter(Filter filter, Range<Integer> range, boolean participants, boolean observers) {
        this.range = range;
        if(!observers) {
            filter = ParticipatingFilter.PARTICIPATING.and(filter);
        }
        if(!participants) {
            filter = ParticipatingFilter.OBSERVING.and(filter);
        }
        this.filter = filter;
    }

    @Override
    public Stream<? extends FeatureDefinition> dependencies() {
        return Stream.of(filter);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean matches(IMatchQuery query) {
        return query.feature(this).response();
    }

    @Override
    public Reactor createFeature(Match match) {
        return new Reactor(match);
    }

    @Override
    public void load(Match match) {
        match.features().get(this);
    }

    class Reactor implements Feature<PlayerCountFilter>, FilterListener<MatchPlayer> {

        private final FilterMatchModule fmm;
        private final Set<MatchPlayer> players = new HashSet<>();

        Reactor(Match match) {
            this.fmm = match.needMatchModule(FilterMatchModule.class);
            fmm.onChange(MatchPlayer.class, filter, this);
        }

        @Override
        public PlayerCountFilter getDefinition() {
            return PlayerCountFilter.this;
        }

        boolean response() {
            return range.contains(players.size());
        }

        @Override
        public void filterQueryChanged(MatchPlayer filterable, boolean response) {
            final boolean before = response();

            if(response) {
                players.add(filterable);
            } else {
                players.remove(filterable);
            }

            if(before != response()) {
                fmm.invalidate(filterable.getMatch());
            }
        }
    }
}
