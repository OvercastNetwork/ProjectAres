package tc.oc.api.tourney;

import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Tournament;
import tc.oc.api.docs.team;
import tc.oc.api.model.QueryService;

public interface TournamentService extends QueryService<Tournament> {

    ListenableFuture<RecordMatchResponse> recordMatch(Tournament tournament, String matchId);

    ListenableFuture<Entrant> entrant(String tournamentId, String teamId);

    default ListenableFuture<Entrant> entrant(Tournament tournament, team.Id team) {
        return entrant(tournament._id(), team._id());
    }

    ListenableFuture<Entrant> entrantByTeamName(String tournamentId, String teamName);

    default ListenableFuture<Entrant> entrantByTeamName(Tournament tournament, String teamName) {
        return entrantByTeamName(tournament._id(), teamName);
    }

    ListenableFuture<Entrant> entrantByMember(String tournamentId, PlayerId playerId);

    default ListenableFuture<Entrant> entrantByMember(Tournament tournament, PlayerId playerId) {
        return entrantByMember(tournament._id(), playerId);
    }
}
