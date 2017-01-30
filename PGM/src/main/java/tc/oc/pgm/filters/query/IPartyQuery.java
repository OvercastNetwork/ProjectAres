package tc.oc.pgm.filters.query;

import java.util.Optional;

import tc.oc.commons.core.util.Optionals;
import tc.oc.pgm.filters.Filterable;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.match.Party;

public interface IPartyQuery extends IMatchQuery {

    Party getParty();

    @Override
    default Match getMatch() {
        return getParty().getMatch();
    }

    @Override
    default int randomSeed() {
        return getParty().hashCode();
    }

    @Override
    default <Q extends Filterable<?>> Optional<? extends Q> filterable(Class<Q> type) {
        return Optionals.first(Optionals.cast(getParty(), type),
                               IMatchQuery.super.filterable(type));
    }

    default boolean isParticipating() {
        // Check the MatchState through the query, so that PlayerMatchQuery can override it.
        return matchState() == MatchState.Running && getParty().isParticipatingType();
    }
}
