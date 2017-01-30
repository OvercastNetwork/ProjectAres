package tc.oc.api.minecraft.sessions;

import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.SessionDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.minecraft.users.OnlinePlayers;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.api.model.NullModelService;
import tc.oc.api.sessions.SessionService;
import tc.oc.api.sessions.SessionStartRequest;
import tc.oc.minecraft.api.entity.Player;

@Singleton
public class LocalSessionService extends NullModelService<Session, SessionDoc.Partial> implements SessionService {

    @Inject private OnlinePlayers<Player> onlinePlayers;
    @Inject private UserStore<Player> userStore;
    @Inject private LocalSessionFactory factory;

    @Override
    public ListenableFuture<Session> start(SessionStartRequest request) {
        return Futures.immediateFuture(factory.newSession(request::player_id, request.ip()));
    }

    @Override
    public ListenableFuture<?> finish(Session session) {
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<Session> online(UserId userId) {
        return onlinePlayers.byUserId(userId)
                            .flatMap(userStore::session)
                            .map(Futures::immediateFuture)
                            .orElseGet(() -> Futures.immediateFailedFuture(new NotFound()));
    }

    @Override
    public ListenableFuture<FindMultiResponse<Session>> friends(UserId player) {
        return Futures.immediateFuture(Collections::emptyList);
    }

    @Override
    public ListenableFuture<FindMultiResponse<Session>> staff(ServerDoc.Network network, boolean disguised) {
        return Futures.immediateFuture(Collections::emptyList);
    }
}
