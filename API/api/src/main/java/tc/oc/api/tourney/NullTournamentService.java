package tc.oc.api.tourney;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Tournament;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.model.NullQueryService;

public class NullTournamentService extends NullQueryService<Tournament> implements TournamentService {

    @Override
    public ListenableFuture<RecordMatchResponse> recordMatch(Tournament tournament, String matchId) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<Entrant> entrant(String tournamentId, String teamId) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<Entrant> entrantByTeamName(String tournamentId, String teamName) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<Entrant> entrantByMember(String tournamentId, PlayerId playerId) {
        return Futures.immediateFailedFuture(new NotFound());
    }
}
