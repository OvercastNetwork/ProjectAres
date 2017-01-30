package tc.oc.api.ocn;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.SessionDoc;
import tc.oc.api.http.HttpOption;
import tc.oc.api.http.QueryUri;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.model.HttpModelService;
import tc.oc.api.model.ModelMeta;
import tc.oc.api.sessions.SessionService;
import tc.oc.api.sessions.SessionStartRequest;

@Singleton
class OCNSessionService extends HttpModelService<Session, SessionDoc.Partial> implements SessionService {

    @Inject private ModelMeta<Session, SessionDoc.Partial> meta;

    @Override
    public ListenableFuture<Session> start(SessionStartRequest request) {
        return this.client().post(collectionUri("start"), request, Session.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<Session> finish(Session session) {
        return client().post(memberUri(session, "finish"), null, Session.class, HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<Session> online(UserId player) {
        return client().get(UriTemplate.fromTemplate(collectionUri("online") + "/{player}").set("player", player.player_id()).expand(),
                            Session.class,
                            HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<FindMultiResponse<Session>> friends(UserId player) {
        return client().get(UriTemplate.fromTemplate(collectionUri("friends") + "/{player}").set("player", player.player_id()).expand(),
                            meta.multiResponseType(),
                            HttpOption.INFINITE_RETRY);
    }

    @Override
    public ListenableFuture<FindMultiResponse<Session>> staff(ServerDoc.Network network, boolean disguised) {
        return client().get(new QueryUri(collectionUri())
                                .put("network", network)
                                .put("staff", true)
                                .put("online", true)
                                .put("disguised", disguised)
                                .encode(),
                            meta.multiResponseType(),
                            HttpOption.INFINITE_RETRY);
    }
}
