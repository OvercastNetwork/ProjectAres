package tc.oc.api.sessions;

import java.util.Collections;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import tc.oc.api.docs.Session;
import tc.oc.api.docs.UserId;
import tc.oc.api.docs.virtual.ServerDoc;
import tc.oc.api.docs.virtual.SessionDoc;
import tc.oc.api.exceptions.NotFound;
import tc.oc.api.message.types.FindMultiResponse;
import tc.oc.api.model.NullModelService;

public class NullSessionService extends NullModelService<Session, SessionDoc.Partial> implements SessionService {

    @Override
    public ListenableFuture<Session> start(SessionStartRequest request) {
        return Futures.immediateFailedFuture(new NotFound());
    }

    @Override
    public ListenableFuture<?> finish(Session session) {
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<Session> online(UserId player) {
        return Futures.immediateFailedFuture(new NotFound());
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
