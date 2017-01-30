package tc.oc.pgm.filters.query;

import java.util.Optional;
import java.util.stream.Stream;

import java.time.Duration;
import tc.oc.api.docs.UserId;
import tc.oc.pgm.features.Feature;
import tc.oc.pgm.features.FeatureFactory;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.match.Competitor;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchModule;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchState;

public interface IMatchQuery extends IQuery {

    Match getMatch();

    @Override
    default int randomSeed() {
        return getMatch().hashCode();
    }

    default <Q extends Filterable<?>> Optional<? extends Q> filterable(Class<Q> type) { return Optional.of((Q) getMatch()); }

    default MatchState matchState() { return getMatch().matchState(); }

    default Duration runningTime() { return getMatch().runningTime(); }

    default Optional<MatchPlayer> player(UserId userId) { return getMatch().player(userId); }

    default Stream<MatchPlayer> players() { return getMatch().players(); }

    default Optional<MatchPlayer> participant(UserId userId) { return getMatch().participant(userId); }

    default Stream<MatchPlayer> participants() { return getMatch().participants(); }

    default Stream<MatchPlayer> observers() { return getMatch().observers(); }

    default Stream<Competitor> competitors() { return getMatch().competitors(); }

    default <T extends MatchModule> Optional<T> module(Class<T> moduleType) { return getMatch().module(moduleType); }

    default  <T extends Feature<?>> T feature(FeatureFactory<T> factory) { return getMatch().feature(factory); }
}
