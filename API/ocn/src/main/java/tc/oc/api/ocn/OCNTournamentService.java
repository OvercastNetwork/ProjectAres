package tc.oc.api.ocn;

import javax.inject.Singleton;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Entrant;
import tc.oc.api.docs.PlayerId;
import tc.oc.api.docs.Tournament;
import tc.oc.api.http.HttpOption;
import tc.oc.api.model.HttpQueryService;
import tc.oc.api.tourney.RecordMatchResponse;
import tc.oc.api.tourney.TournamentService;

@Singleton
class OCNTournamentService extends HttpQueryService<Tournament> implements TournamentService {

    @Override
    public ListenableFuture<RecordMatchResponse> recordMatch(Tournament tournament, String matchId) {
        return client().post(memberUri(tournament._id(), "record_match"),
                             ImmutableMap.of("match_id", matchId),
                             RecordMatchResponse.class,
                             HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<Entrant> entrant(String tournamentId, String teamId) {
        return client().get(UriTemplate.fromTemplate("/tournaments/{id}/entrants/{team_id}")
                                       .set("id", tournamentId)
                                       .set("team_id", teamId)
                                       .expand(),
                            Entrant.class);
    }

    private ListenableFuture<Entrant> entrantSearch(String tournamentId, String param, String value) {
        return client().get(UriTemplate.fromTemplate("/tournaments/{id}/entrants?{param}={value}")
                                       .set("id", tournamentId)
                                       .set("param", param)
                                       .set("value", value)
                                       .expand(),
                            Entrant.class);
    }

    @Override
    public ListenableFuture<Entrant> entrantByTeamName(String tournamentId, String teamName) {
        return entrantSearch(tournamentId, "team_name", teamName);
    }

    @Override
    public ListenableFuture<Entrant> entrantByMember(String tournamentId, PlayerId playerId) {
        return entrantSearch(tournamentId, "member_id", playerId._id());
    }
}
